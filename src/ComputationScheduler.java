import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

// Планировщик для операций, который использует FixedThreadPool с 2-мя потоками
public class ComputationScheduler implements Scheduler {

    // Единственный экземпляр планировщика (Singleton). Обеспечит единый пулл потоков для всех операций Computation
    static final ComputationScheduler INSTANCE = new ComputationScheduler();

    // Фиксированный пул потоков
    private final ExecutorService executor;

    // Конструктор для реализации Singleton, который создает FixedThreadPool с 2 потоками и кастомной фабрикой
    private ComputationScheduler() {
        executor = Executors.newFixedThreadPool(2, new java.util.concurrent.ThreadFactory() {
            // Фабрика для создания именованных потоков
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "Computation-Thread");
                return thread;
            }
        });
    }

    // Выполняем задачу в фиксированном пуле потоков
    public void execute(Runnable task) {
        executor.execute(task);
    }

    // Завершает работу планировщика, инициируя корректное завершение всех потоков
    public void shutdown() {
        executor.shutdown();
    }
}