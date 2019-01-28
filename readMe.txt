Some info: this app gives ability configure number of max threads. So when breadth first searching algorithm is going through
graph of links(urls) threads are creating by main thread while their count in thread queue < maxThreadsCount. While number of active
threads = maxThreadsCount main thread is waiting. So in this situation one-stage node-threads can be started in one by one.
But if count of nodes on the same stage>= maxThreadsCount node-threads will start "together"

How it can be tested: Fest(I think it's better) or Jemmy