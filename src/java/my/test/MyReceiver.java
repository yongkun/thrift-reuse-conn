package my.test;

import my.thrift.SendData;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;

import org.apache.log4j.Logger;


public class MyReceiver implements Runnable {
	private static final Logger LOG = Logger.getLogger(MyReceiver.class);
	private final SendData.Processor processor;
	private final TProtocol protocol;

	public int pollingInterval = 0; // in ms
	public int reconnectInterval = 5000; // in ms

	public MyReceiver(TProtocol protocol, SendData.Iface sendData) {
		this.protocol = protocol;
		this.processor = new SendData.Processor(sendData);
	}

	@Override
	public void run() {
		while (true) {
			try {
				while (processor.process(protocol, protocol) == true) {

					try {
						if (this.pollingInterval > 0) {
							Thread.sleep(this.pollingInterval);
						}
					} catch (InterruptedException ie) {
						LOG.warn(ie.getMessage());
						break;
					}

				}
			} catch (TException e) {
				LOG.warn("Connection is not available, sleep "
						+ this.reconnectInterval + " to reconnect...\n");
				try {
					if (this.reconnectInterval > 0) {
						Thread.sleep(this.reconnectInterval);
					}
				} catch (InterruptedException ie) {
					LOG.warn(ie.getMessage());
					break;
				}
			}
		}
	}
}
