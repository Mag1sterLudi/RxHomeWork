import org.junit.Test;
import org.junit.Assert;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

// Юнит-тесты с использованием junit для проверки функционирования моей программы
public class UnitTests {


    //Тест базовой функциональности: создание Observable и подписка Observer.
    // Проверяет отправку данных и корректное завершение потока.
    @Test
    public void testObservableCreation() {
        AtomicInteger counter = new AtomicInteger(0); // Потокобезопасный счетчик элементов
        CountDownLatch latch = new CountDownLatch(1); // Синхронизатор для ожидания завершения

        Observable.create(new Observable.Source<String>() {
            public void subscribe(Observer<String> observer) {
                observer.onNext("test"); // Отправляем элемент
                observer.onComplete(); // Завершаем поток
            }
        }).subscribe(new Observer<String>() {
            public void onNext(String item) {
                counter.incrementAndGet(); // Считаем полученные элементы
            }
            public void onError(Throwable t) {}
            public void onComplete() {
                latch.countDown(); // Разблокируем ожидание
            }
        });

        try { latch.await(1, TimeUnit.SECONDS); } catch (InterruptedException e) {}

        Assert.assertEquals("Observable should emit exactly 1 item", 1, counter.get());
    }

    /**
     * Тест оператора filter: фильтрация элементов по условию.
     * Проверяет что только четные числа проходят через фильтр.
     */
    // Тест оператора filter - фильтрация элементов по условию
    // Проверяет, что только четные числа проходят через фильтр
    @Test
    public void testFilterOperator() {
        AtomicInteger receivedCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);

        Observable.create(new Observable.Source<Integer>() {
            public void subscribe(Observer<Integer> observer) {
                observer.onNext(1); // нечетное - отфильтруется
                observer.onNext(2); // четное - пройдет и тд
                observer.onNext(3);
                observer.onNext(4);
                observer.onComplete();
            }
        }).filter(new java.util.function.Predicate<Integer>() {
            public boolean test(Integer x) {
                return x % 2 == 0; // Фильтр для четных чисел
            }
        }).subscribe(new Observer<Integer>() {
            public void onNext(Integer item) {
                receivedCount.incrementAndGet(); // Считаем прошедшие фильтр
            }
            public void onError(Throwable t) {}
            public void onComplete() {
                latch.countDown();
            }
        });

        try { latch.await(1, TimeUnit.SECONDS); } catch (InterruptedException e) {}

        Assert.assertEquals("Filter should pass exactly 2 even numbers", 2, receivedCount.get());
    }

    // Тестирование оператора map - преобразование типов данных
    // Проверяем изменение интеджера на стринг с добавлением префикса
    @Test
    public void testMapOperator() {
        AtomicReference<String> result = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Observable.create(new Observable.Source<Integer>() {
            public void subscribe(Observer<Integer> observer) {
                observer.onNext(5); // Исходное число
                observer.onComplete();
            }
        }).map(new java.util.function.Function<Integer, String>() {
            public String apply(Integer x) {
                return "Number: " + x; // Преобразование 5 -> "Number: 5"
            }
        }).subscribe(new Observer<String>() { // Теперь Observer<String>
            public void onNext(String item) {
                result.set(item); // Сохраняем результат преобразования
            }
            public void onError(Throwable t) {}
            public void onComplete() {
                latch.countDown();
            }
        });

        try { latch.await(1, TimeUnit.SECONDS); } catch (InterruptedException e) {}

        Assert.assertEquals("Map should transform correctly", "Number: 5", result.get());
    }

    /**
     * Тест оператора subscribeOn: переключение на другой поток.
     * Проверяет что Observable выполняется НЕ в main потоке.
     */
    // Тест оператора subscribeOn - переключение на другой поток
    // Проверяет что Observable выполняется не в потоке main.
    @Test
    public void testSubscribeOn() {
        AtomicReference<String> threadName = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        String mainThread = Thread.currentThread().getName(); // Запоминаем main поток
        System.out.println("Main thread: " + mainThread);

        Observable.create(new Observable.Source<String>() {
                    public void subscribe(Observer<String> observer) {
                        String currentThread = Thread.currentThread().getName();
                        System.out.println("Observable thread: " + currentThread);
                        threadName.set(currentThread); // Сохраняем имя потока выполнения
                        observer.onNext("data");
                        observer.onComplete();
                    }
                }).subscribeOn(Scheduler.io()) // переключение на IO планировщик
                .subscribe(new Observer<String>() {
                    public void onNext(String item) {
                        System.out.println("OnNext thread: " + Thread.currentThread().getName());
                    }
                    public void onError(Throwable t) {}
                    public void onComplete() {
                        latch.countDown();
                    }
                });

        try { latch.await(1, TimeUnit.SECONDS); } catch (InterruptedException e) {}

        String thread = threadName.get();
        System.out.println("Final thread name: " + thread);

        // Проверяем что выполнение НЕ в main потоке
        Assert.assertTrue("Should run on different thread than main",
                thread != null && !thread.equals(mainThread));
    }

    // Тестирование Disposable, который управляет жизненным циклом подписки
    // Проверяем состояние до и после отмены подписки
        @Test
    public void testDisposableState() {
        // Получаем Disposable объект для управления подпиской
        Disposable disposable = Observable.create(new Observable.Source<String>() {
            public void subscribe(Observer<String> observer) {
                observer.onNext("test");
                observer.onComplete();
            }
        }).subscribe(new Observer<String>() {
            public void onNext(String item) {}
            public void onError(Throwable t) {}
            public void onComplete() {}
        });

        Assert.assertFalse("Should not be disposed initially", disposable.isDisposed());

        disposable.dispose(); // Отменяем подписку

        Assert.assertTrue("Should be disposed after dispose()", disposable.isDisposed());
    }
}