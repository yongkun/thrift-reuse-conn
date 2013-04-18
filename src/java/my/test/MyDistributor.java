package my.test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import my.thrift.SendData;
import my.thrift.SendData.Iface;

import org.apache.log4j.Logger;

import org.apache.thrift.TException;


public class MyDistributor implements /* Iface, */Runnable {
	private static final Logger LOG = Logger.getLogger(MyDistributor.class);
	private final BlockingQueue<String> dataQueue;
	private final List<SendData.Client> clients;
	private long sendingCounter = 0;
	private long batchCounter = 0;

	// number of records in sending queue to be sent in batch
	public int linesPerRPC = 1;
	// if this timeout elapsed, the data in queue must be sent
	public int queueWaitingTimeout = 5000;
	public int reconnectInterval = 5000; // in ms

	public MyDistributor() {
		this.dataQueue = new LinkedBlockingQueue<String>();
		this.clients = new ArrayList<SendData.Client>();
	}

	public synchronized void addClient(SendData.Client client) {
		clients.add(client);

		LOG.info("Add client connection.");
	}

	public synchronized void addData(String data) {
		dataQueue.add(data);
		LOG.debug("Added data to sending queue: " + data);
	}

	public synchronized void addList(List<String> dataList) {
		dataQueue.addAll(dataList);
	}

	@Override
	public void run() {
		while (true) {
			try {
				String data = "";

				long startTime = System.currentTimeMillis();
				List<String> dataList = new ArrayList<String>();
				int counter = 0;
				while (counter < this.linesPerRPC
						&& System.currentTimeMillis() - startTime < this.queueWaitingTimeout) {
					data = dataQueue.take();
					dataList.add(data);
					counter++;
				}
				this.sendingCounter += counter;
				this.batchCounter++;
				LOG.debug("Total: " + this.sendingCounter + ", Batch: "
						+ this.batchCounter + ", Queue size: "
						+ dataList.size());

				Iterator<SendData.Client> clientItr = clients.iterator();
				while (!clientItr.hasNext()) {
					LOG.info("No connection available, waiting...");
					try {
						if (this.reconnectInterval > 0) {
							Thread.sleep(this.reconnectInterval);
						}
					} catch (InterruptedException ie) {
						LOG.warn(ie.getMessage());
					}
					clientItr = clients.iterator();
				}

				while (clientItr.hasNext()) {
					SendData.Client client = clientItr.next();
					try {
						if (this.linesPerRPC > 1) {
							client.sendList(dataList);
						} else {
							client.sendOne(data);
						}
					} catch (TException te) {
						clientItr.remove();
						LOG.info("Connection is not available, remove it from list.");
					} catch (Exception e) {
						LOG.error(e.getMessage());
					}
				}

			} catch (InterruptedException ie) {
				LOG.warn(ie.getMessage());
			}
		}
	}

}
