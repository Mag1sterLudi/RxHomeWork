import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

// Планировщик с единственным потоком выполнения, использует SingleThreadExecutor, который
    // генерирует последовательное выполннеие всех задач в одном потоке
    // Необходим для операций, требующих строго порядка выполнения или синхронизации
public class SingleThreadScheduler implements Scheduler {

    // Единственный экземпляр планировщика (Singleton)
    static final SingleThreadScheduler INSTANCE = new SingleThreadScheduler();

    // Исполнитель с единственным потоком
    private final ExecutorService executor;

    // Конструктор, который создает SingleThreadExecutor с кастомной фабрикой,
    // присваивающей имена
    private SingleThreadScheduler() {
        executor = Executors.newSingleThreadExecutor(new java.util.concurrent.ThreadFactory() {
            // Фабрика для создания именованного единственного потока
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "Single-Thread");
                return thread;
            }
        });
    }

    // Выполняет задачу в единственном потоке (все таски выполняются в порядке поступления),
    // параллельное выполнение отсутствует
    public void execute(Runnable task) {
        executor.execute(task);
    }

    // Завершает работу планировщика с единственным потоком
    // Останавливает прием новых задач и корректно завершает рабочий поток
    public void shutdown() {
        executor.shutdown();
    }
}