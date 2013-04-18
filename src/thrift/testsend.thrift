/*
 * created by Yongkun Wang on 2011-Dec-22
 */

namespace java my.thrift

service SendData {
  oneway void sendOne(1: string data);
  oneway void sendList(1: list<string> dataList);
}
