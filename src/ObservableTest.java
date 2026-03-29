import java.util.function.Function;
import java.util.function.Predicate;

// Класс для тестирования функциональности моей реализации реактивной Java
    // Содержит набор тестов для проверки всех ключевых компонентов, базового функционала,
    // операторов, планировщиков и управления подписками
public class ObservableTest {

    // Запуск полного набора тестов
    public static void runTests() {
        System.out.println("- Простые тесты -\n");

        testBasic(); // Тест базовой функциональности
        testOperators(); // Тест операторов преобразования
        testSchedulers(); // Тест планировщиков IO и Computation
        testSingleScheduler(); // Тест единого планировщика
        testDisposable(); // Тест управления подписками

        // Завершаем работу всех планировщиков после выполнения тестов
        Scheduler.io().shutdown();
        Scheduler.computation().shutdown();
        Scheduler.single().shutdown();

        System.out.println("- Тесты завершены -\n");
    }

    // Тест базового функционала Observable и Observer
    // В частности, проверяет создание Observable, подписку и получение событий onNext и
    // завершение потока через onComplete
    public static void testBasic() {
        System.out.println("Тест 1: Базовый функционал");

        // Создаем простой Observable с двумя строковыми элементами
        Observable.create(new Observable.Source<String>() {
            public void subscribe(Observer<String> observer) {
                observer.onNext("Hello"); // Отправляем первый элемент
                observer.onNext("World"); // Отправляем второй элемент
                observer.onComplete(); // Завершаем поток
            }
        }).subscribe(new Observer<String>() {
            // Обрабатываем каждый полученный элемент
            public void onNext(String item) {
                System.out.println("  Получено: " + item);
            }
            // Обрабатываем ошибки (в данном тесте не ожидаются)
            public void onError(Throwable t) {
                System.out.println("  Ошибка: " + t.getMessage());
            }
            // Обрабатываем завершение потока
            public void onComplete() {
                System.out.println("  Завершено");
            }
        });
        System.out.println();
    }

    // Тест операторов для преобразования данных - filter и map
    // Проверяет - фильтрацию чётных чисел, преобразование чисел в строки с префиксом, последовательное
    // применение операторов в цепочке, доставку отфильтрованных и преобразованных элементов подписчику
    public static void testOperators() {
        System.out.println("Тест 2: Операторы map и filter");

        Observable.create(new Observable.Source<Integer>() {
                    // Генерируем последовательность чисел от 1 до 5
                    public void subscribe(Observer<Integer> observer) {
                        for (int i = 1; i <= 5; i++) {
                            observer.onNext(i);
                        }
                        observer.onComplete();
                    }
                })
                // Фильтруем только четные числа
                .filter(new Predicate<Integer>() {
                    public boolean test(Integer x) {
                        return x % 2 == 0;  // Пропускаем только четные
                    }
                })
                // Преобразуем числа в строки с описанием
                .map(new Function<Integer, String>() {
                    public String apply(Integer x) {
                        return "Четное: " + x;
                    }
                })
                // Подписываемся на результат цепочки операторов
                .subscribe(new Observer<String>() {
                    public void onNext(String item) {
                        System.out.println("  " + item);
                    }
                    public void onError(Throwable t) {
                        System.out.println("  Ошибка: " + t.getMessage());
                    }
                    public void onComplete() {
                        System.out.println("  Операторы работают");
                    }
                });
        System.out.println();
    }

    // Тест планировщиков IO и Computation, который проверяет, выполнение подписки в потоке планировщика IO и
    // обработку событий в потоке планировщика Computation
    public static void testSchedulers() {
        System.out.println("Тест 3: Планировщики IO и Computation");

        Observable.create(new Observable.Source<String>() {
                    // Источник данных, который показывает поток выполнения
                    public void subscribe(Observer<String> observer) {
                        System.out.println("  Поток источника: " + Thread.currentThread().getName());
                        observer.onNext("Данные");
                        observer.onComplete();
                    }
                })
                .subscribeOn(Scheduler.io()) // Подписка выполняется в IO потоке
                .observeOn(Scheduler.computation()) // Обработка в вычислительном потоке
                .subscribe(new Observer<String>() {
                    public void onNext(String item) {
                        // Демонстрирует, в каком потоке происходит обработка
                        System.out.println("  Поток обработки: " + Thread.currentThread().getName());
                        System.out.println("  Данные: " + item);
                    }
                    public void onError(Throwable t) {
                        System.out.println("  Ошибка: " + t.getMessage());
                    }
                    public void onComplete() {
                        System.out.println("  IO и Computation работают");
                    }
                });

        // Время для завершения асинхронных операций
        try { Thread.sleep(200); } catch (InterruptedException e) {}
        System.out.println();
    }

    // Тест планировщика с единственным потоком, который проверяет выполнение подписки и обработки
    // в одном потоке
    public static void testSingleScheduler() {
        System.out.println("Тест 4: Single Thread Scheduler");

        Observable.create(new Observable.Source<String>() {
                    // Источник данных для тестирования единого планировщика
                    public void subscribe(Observer<String> observer) {
                        System.out.println("  Источник в потоке: " + Thread.currentThread().getName());
                        observer.onNext("Single Thread Test");
                        observer.onComplete();
                    }
                })
                .subscribeOn(Scheduler.single())       // Подписка в едином потоке
                .observeOn(Scheduler.single())         // Обработка в том же едином потоке
                .subscribe(new Observer<String>() {
                    public void onNext(String item) {
                        // Проверяем, что обработка происходит в едином потоке
                        System.out.println("  Обработка в потоке: " + Thread.currentThread().getName());
                        System.out.println("  Данные: " + item);
                    }
                    public void onError(Throwable t) {
                        System.out.println("  Ошибка: " + t.getMessage());
                    }
                    public void onComplete() {
                        System.out.println("  Single Scheduler работает");
                    }
                });

        // Ожидаем завершения асинхронных операций
        try { Thread.sleep(200); } catch (InterruptedException e) {}
        System.out.println();
    }

    // Тест механизма для управления подписками, проверяет возможность отмены подписки и
    // прекращения получения событий
    public static void testDisposable() {
        System.out.println("Тест 5: Disposable");

        // Создаем Observable, который генерирует последовательность чисел с задержкой
        Disposable disposable = Observable.create(new Observable.Source<Integer>() {
            public void subscribe(Observer<Integer> observer) {
                // Генерируем 10 чисел с паузами
                for (int i = 1; i <= 10; i++) {
                    observer.onNext(i);
                    // Пауза между элементами для демонстрации отмены
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        break;  // Прерываем при отмене потока
                    }
                }
                observer.onComplete();
            }
        }).subscribe(new Observer<Integer>() {
            public void onNext(Integer item) {
                System.out.println("  Получено: " + item);
            }
            public void onError(Throwable t) {
                System.out.println("  Ошибка: " + t.getMessage());
            }
            public void onComplete() {
                System.out.println("  Поток завершен");
            }
        });

        // Даем время получить несколько элементов
        try { Thread.sleep(200); } catch (InterruptedException e) {}

        // Проверяем и отменяем подписку
        if (!disposable.isDisposed()) {
            disposable.dispose();
            System.out.println("  Подписка отменена");
        }

        // Проверяем состояние Disposable после отмены
        System.out.println("  Disposed: " + disposable.isDisposed());
        System.out.println();
    }
}