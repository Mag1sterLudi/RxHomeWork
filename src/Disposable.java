
// Интерфейс Disposable предоставляет механизм для отмены подписки на Observable
public interface Disposable {

    // Отменяет подписку и освобождает ресурсы, после этого метода Observer больше не будет получать события
    void dispose();

    // Проверяет, была ли отменена подписка или нет
    boolean isDisposed();
}