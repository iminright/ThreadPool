package com.im.threadpool;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPool {
	private final int POOL_SIZE ;
	private LinkedList<Runnable> jobs = new LinkedList<Runnable>() ;
	private List<Work> works = new ArrayList<Work>() ;
	
	public ThreadPool(int size){
		this.POOL_SIZE = size ;
		initWorkThread() ;
	}
	
	private void initWorkThread(){
		for(int i = 0; i < POOL_SIZE; i++){
			Work work = new Work() ;
			works.add(work) ;
			new Thread(work, "workThread-" + i).start();
		}
	}
	
	public void exec(Runnable job){
		synchronized (jobs) {
			jobs.add(job) ;
			jobs.notify() ;
		}
	}
	
	class Work implements Runnable{
		
		private volatile boolean isRun = true ;
		
		@Override
		public void run() {
			while(isRun){
				Runnable job = null ;
				synchronized (jobs) {
					while(jobs.isEmpty()){
						try {
							jobs.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					job = jobs.removeFirst() ;
				}
				if(job != null){
					job.run();
				}
			}
		}
		
		public void shutdown(){
			isRun = false ;
		}
		
	}
	
	public static void main(String args[]){
		ThreadPool pool = new ThreadPool(5) ;
		final AtomicInteger count = new AtomicInteger(0) ;
		for(int i = 0; i < 100; i++){
			if(i == 50){
				System.out.println("shutdown workThread-4");
				pool.works.get(4).shutdown();
			}
			pool.exec(new Runnable() {
				
				@Override
				public void run() {
					System.out.println(Thread.currentThread().getName() + ":" + count.getAndIncrement()) ;
				}
			});
		}
		
	}
	
	
	
}
