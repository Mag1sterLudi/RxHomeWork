import java.util.function.Function;
import java.util.function.Predicate;

// Класс Observable предоставляющий источник данных, на который можно подписаться
    // Позволяет создавать потоки данных и применять к ним различные операторы (map, filter и тд)
// Позволяет управлять подписками через Disposable
public class Observable<T> {

    // Источник данных для этого Observable
    private final Source<T> source;

    // Конструктор
    public Observable(Source<T> source) {
        this.source = source;
    }

    // Статический фабричный метод для создания Observable из источника данных
    public static <T> Observable<T> create(Source<T> source) {
        return new Observable<>(source);
    }

    // Метод, который позволяет подписать Observer на Observable и
    // и возвращает Disposable, который позволяет управлять (в т.ч. и отменять) подписку
    // Обрабатывает исключения, возникающие при подписке
    public Disposable subscribe(Observer<T> observer) {
        // Флажки для отслеживания состояния подписки
        boolean[] disposed = {false};

        // Создаем Disposable для управления подпиской
        Disposable disposable = new Disposable() {
            public void dispose() {
                disposed[0] = true;
            }
            public boolean isDisposed() {
                return disposed[0];
            }
        };

        try {
            // Оборачиваем observer для проверки состояния disposed
            Observer<T> wrappedObserver = new Observer<T>() {
                public void onNext(T item) {
                    if (!disposed[0]) {  // Проверка не отменена ли подписка
                        observer.onNext(item);
                    }
                }
                public void onError(Throwable t) {
                    if (!disposed[0]) {
                        observer.onError(t);
                    }
                }
                public void onComplete() {
                    if (!disposed[0]) {
                        observer.onComplete();
                    }
                }
            };

            // Запускаем источник данных
            source.subscribe(wrappedObserver);
        } catch (Exception e) {
            // Если произошла ошибка при подписке, передаем ее observer
            if (!disposed[0]) {
                observer.onError(e);
            }
        }

        return disposable;
    }

    // Оператор map преобразует каждый элемент с помощью заданной функции
    // Для каждого элемента из исходного Observable применяет mapper и передаёт результат в новый Observable
    // Также обрабатывает искючение, которое передаётся в onError
    public <R> Observable<R> map(Function<T, R> mapper) {
        return create(new Source<R>() {
            public void subscribe(Observer<R> observer) {
                // Подписываемся на исходный Observable
                source.subscribe(new Observer<T>() {
                    public void onNext(T item) {
                        try {
                            // Применяем функцию преобразования
                            R result = mapper.apply(item);
                            observer.onNext(result);
                        } catch (Exception e) {
                            // Если преобразование вызвало ошибку
                            observer.onError(e);
                        }
                    }
                    public void onError(Throwable t) {
                        observer.onError(t);
                    }
                    public void onComplete() {
                        observer.onComplete();
                    }
                });
            }
        });
    }

    // Оператор filter пропускает только те элементы, которые удовлетворяют условию
    // Элементы, для которых predicate.test() возвращает true, передаются дальше, остальные игнорируются
    // predicate - условие для фильтрации элементов
    // Исключения передаются через onError
    public Observable<T> filter(Predicate<T> predicate) {
        return create(new Source<T>() {
            public void subscribe(Observer<T> observer) {
                source.subscribe(new Observer<T>() {
                    public void onNext(T item) {
                        try {
                            // Проверяем условие фильтрации
                            if (predicate.test(item)) {
                                observer.onNext(item);  // Пропускаем элемент дальше
                            }
                            // Если условие не выполнено, элемент игнорируется
                        } catch (Exception e) {
                            observer.onError(e);
                        }
                    }
                    public void onError(Throwable t) {
                        observer.onError(t);
                    }
                    public void onComplete() {
                        observer.onComplete();
                    }
                });
            }
        });
    }

    // Оператор flatMap преобразует каждый элемен в новый Observable и подписывается на него
    // События из всех внутренних Observable объединяются в один поток
    public <R> Observable<R> flatMap(Function<T, Observable<R>> mapper) {
        return create(new Source<R>() {
            public void subscribe(Observer<R> observer) {
                source.subscribe(new Observer<T>() {
                    public void onNext(T item) {
                        try {
                            // Преобразуем элемент в новый Observable
                            Observable<R> innerObservable = mapper.apply(item);
                            // Подписываемся на внутренний Observable
                            innerObservable.subscribe(observer);
                        } catch (Exception e) {
                            observer.onError(e);
                        }
                    }
                    public void onError(Throwable t) {
                        observer.onError(t);
                    }
                    public void onComplete() {
                        observer.onComplete();
                    }
                });
            }
        });
    }

    // Планировщик для выполнения подписок, определяет в каком потоке будет выполняться источник данных
    public Observable<T> subscribeOn(Scheduler scheduler) {
        return create(new Source<T>() {
            public void subscribe(Observer<T> observer) {
                // Запускаем подписку в заданном планировщике
                scheduler.execute(new Runnable() {
                    public void run() {
                        source.subscribe(observer);
                    }
                });
            }
        });
    }


    // Планировщик для обработки событий (observerOn)
    // Каждое событие выполняется в потоке, управляемомо scheduler
    public Observable<T> observeOn(Scheduler scheduler) {
        return create(new Source<T>() {
            public void subscribe(Observer<T> observer) {
                source.subscribe(new Observer<T>() {
                    // Каждое событие выполняем в заданном планировщике
                    public void onNext(T item) {
                        scheduler.execute(new Runnable() {
                            public void run() {
                                observer.onNext(item);
                            }
                        });
                    }
                    public void onError(Throwable t) {
                        scheduler.execute(new Runnable() {
                            public void run() {
                                observer.onError(t);
                            }
                        });
                    }
                    public void onComplete() {
                        scheduler.execute(new Runnable() {
                            public void run() {
                                observer.onComplete();
                            }
                        });
                    }
                });
            }
        });
    }
    // Источник данных для Observable
    // Интерфейс Source определяет логику генерации элементов
    public interface Source<T> {

        // Метод подписки, который определяет логи генерации данных
        void subscribe(Observer<T> observer);
    }
}