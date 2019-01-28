Some info: this app gives ability configure number of max threads. So when breadth first searching algorithm is going through
graph of links(urls) threads are creating by main thread while their count in thread queue < maxThreadsCount. While number of active
threads = maxThreadsCount main thread is waiting. So in this situation one-stage node-threads can be started in one by one.
But if count of nodes on the same stage>= maxThreadsCount node-threads will start "together"

All links must be started from https not http, becouse i tested only on https. There can appear problems with auto-redirection from http to
https. App reads robot.txt on websites to create list of "bad urls", they will not be loaded.


How it can be tested: Fest(I think it's better) or Jemmy

How to build: mvn clean install

P.S. More logs must be added. There are only few I used to test system.