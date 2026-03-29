import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

// Планировщик для IO операций, который использует CachedThreadPool, который создает новые потоки по мере необходимости
    // и переиспользует существующие (подойдет для чтения файлов, сетевых запросов и тд)
    // уничтожает потоки спустя 60 секунд бездействия
public class IOThreadScheduler implements Scheduler {

    // Единственный экземпляр планировщика (Singleton), гарантирует единый пулл потоков
    // для всех I/O операций в программе
    static final IOThreadScheduler INSTANCE = new IOThreadScheduler();

    // Пул потоков для выполнения I/O операций
    private final ExecutorService executor;

    // Конструктор для реалзиации Singleton, создающий CachedThreadPool с кастомной фабрикой потоков
    private IOThreadScheduler() {
        executor = Executors.newCachedThreadPool(new java.util.concurrent.ThreadFactory() {
            // Кастомная фабрика для создания потоков с именем
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "IO-Thread");
                return thread;
            }
        });
    }

    // Выполнение задач в пуле IO потоков, CachedThreadPool автоматически управляет
    // количеством потоков
    public void execute(Runnable task) {
        executor.execute(task);
    }

    // Завершает работу планировщика, инициируя корректное завершение всех потоков
    public void shutdown() {
        executor.shutdown();
    }
}