package digital.bauermeister.scantube

class Signal {
    private val signal = java.lang.Object()

    fun send() {
        synchronized(signal) { signal.notifyAll() }
    }

    fun await() {
        synchronized(signal) { signal.wait() }
    }
}

class Callbacks<T> {
    var success: T? = null
    private var error: Throwable? = null
    private val signal = Signal()

    val onSuccess = { result: T ->
        run {
            success = result
            signal.send()
        }
    }

    val onError = { err: Throwable ->
        run {
            error = err
            signal.send()
        }
    }

    fun await() {
        signal.await()
        if (error != null) {
            throw Throwable(error)
        }
    }
}
