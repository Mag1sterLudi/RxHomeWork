import java.util.function.Function;

public class Main {
    // Точка входа в программу
    public static void main(String[] args) {
        System.out.println("My RxJava Library\n");

        // Демонстрация основной функциональности
        basicDemo(); // Показывает базовый функционал - создание потока, подписку и получение событий
        operatorsDemo(); // Демонстрирует цепочку операторов map и filter для преобразования и фильтрации данных

        // Запуск полного набора тестов
        ObservableTest.runTests();

        System.out.println("Программа завершена");

        // Завершаем работу всех планировщиков
        Scheduler.io().shutdown();
        Scheduler.computation().shutdown();
        Scheduler.single().shutdown();

        // Даем время завершиться всем потокам
        try { Thread.sleep(300); } catch (InterruptedException e) {}
        System.exit(0);
    }


    // Стандартное использование Observable, создает источник данных и подписку на него
    private static void basicDemo() {
        System.out.println("- Демо -");
        System.out.println("1. Простой Observable:");

        // Создаем Observable с помощью статического метода create
        Observable.create(new Observable.Source<String>() {
            // Отправляем последовательность элементов в поток
            public void subscribe(Observer<String> observer) {
                observer.onNext("RxJava"); // Отправляем первый элемент
                observer.onNext("работает!"); // Отправляем второй элемент
                observer.onComplete(); // Сигнализируем о завершении потока
            }
        }).subscribe(new Observer<String>() {
            // Подписываемся на поток и обрабатываем события
            public void onNext(String item) {
                System.out.println("  " + item);
            }
            public void onError(Throwable t) {
                System.out.println("  Ошибка: " + t.getMessage());
            }
            public void onComplete() {
                System.out.println("  Готово\n");
            }
        });
    }

    // Работа операторов преобразования данных
    private static void operatorsDemo() {
        System.out.println("2. Цепочка операторов:");

        Observable.create(new Observable.Source<Integer>() {
                    // Создание источника, генерирующего числа от 1 до 4
                    public void subscribe(Observer<Integer> observer) {
                        observer.onNext(1); // Поочередно отправляем числа в поток
                        observer.onNext(2);
                        observer.onNext(3);
                        observer.onNext(4);
                        observer.onComplete(); // Сигнализируем о завершении
                    }
                })
                // Фильтруем только числа больше 2
                .filter(new java.util.function.Predicate<Integer>() {
                    public boolean test(Integer x) {
                        return x > 2;
                    }
                })
                // Преобразуем числа в строки с умножением на 10
                .map(new java.util.function.Function<Integer, String>() {
                    public String apply(Integer x) {
                        return "Результат: " + (x * 10);
                    }
                })
                // Подписываемся на результат
                .subscribe(new Observer<String>() {
                    public void onNext(String item) {
                        System.out.println("  " + item);
                    }
                    public void onError(Throwable t) {
                        System.out.println("  Ошибка: " + t.getMessage());
                    }
                    public void onComplete() {
                        System.out.println("  Цепочка готова\n");
                    }
                });
    }
}