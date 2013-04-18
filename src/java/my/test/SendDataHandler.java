package my.test;

import java.util.List;
import java.util.Iterator;

import my.thrift.SendData;

import org.apache.log4j.Logger;

import org.apache.thrift.TException;


public class SendDataHandler implements SendData.Iface {
	private static final Logger LOG = Logger.getLogger(SendDataHandler.class);

	private long counter = 0;

	@Override
	public void sendOne(String data) throws TException {
		LOG.debug("Get: " + data);
		counter++;
		LOG.debug(System.currentTimeMillis() + " " + counter);
	}

	@Override
	public void sendList(List<String> dataList) throws TException {
		if (dataList.size() > 0) {
			Iterator<String> itr = dataList.iterator();
			while (itr.hasNext()) {
				String data = itr.next();
				LOG.debug("Get: " + data);
				counter++;
				LOG.debug(System.currentTimeMillis() + " " + counter);
			}
		} else {
			LOG.warn("Get an empty list.");
		}
	}

}
