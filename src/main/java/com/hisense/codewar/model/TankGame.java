package com.hisense.codewar.model;

import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.hisense.codewar.player.DemoPlayer;

public class TankGame {
	private static final Logger log = LoggerFactory.getLogger(TankGame.class);
	private String mIp;
	private int mPort;
	private TankGamePlayInterface mPlay;
	private SocketChannel mSocket;
	private FileWriter mWriter = null;

	public TankGame() {
	}

	public ITtank tank_init(String ip, int port, String token, TankGamePlayInterface play, String recorddir) {
		ITtank tank = new ITtank(token, recorddir);
		this.mIp = ip;
		this.mPort = port;
		this.mPlay = play;
		return tank;
	}

	public void record_start(ITtank tank, String blocks) {
		if (tank.getRecordir() == null) {
			return;
		}

		Date date = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		String timeStr = df.format(date);
		StringBuilder recordFileName = new StringBuilder();
		recordFileName.append(tank.getRecordir());
		recordFileName.append("/cwrecord-");
		recordFileName.append(tank.getId());
		recordFileName.append("-");
		recordFileName.append(timeStr);
		recordFileName.append(".rec");
		try {
			if (mWriter != null) {
				mWriter.close();
			}
			mWriter = new FileWriter(recordFileName.toString());
			mWriter.write("BLOCKS: ");
			mWriter.write(blocks);
			mWriter.write("\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if (mWriter != null) {
				try {
					mWriter.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
				}
			}

		}

		System.out.println("recording " + recordFileName.toString() + "...");
	}

	public void record_map(String mapinfo) {
		if (mWriter == null) {
			return;
		}
		try {
			mWriter.write(mapinfo);
			mWriter.write("\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void record_stop() {
		if (mWriter == null) {
			return;
		}
		try {
			mWriter.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
		}
	}

	private void tank_connect(Selector selector) throws IOException {
		mSocket = SocketChannel.open();
		mSocket.configureBlocking(false);
		mSocket.register(selector, SelectionKey.OP_CONNECT);
		mSocket.connect(new InetSocketAddress(mIp, mPort));
	}

	private void send_token(ITtank tank, SocketChannel client) throws IOException {
		String buffer = "TOKEN " + tank.getToken() + "\n";
		client.write(ByteBuffer.wrap(buffer.getBytes()));
		tank.setState(TankGameState.sWait);
	}

	private void update_map(ITtank tank, String mapinfo) {
		List<TankGameInfo> tanks = new ArrayList<TankGameInfo>();
		List<TankMapProjectile> projectiles = new ArrayList<TankMapProjectile>();

		JSONObject jsonObject = JSON.parseObject(mapinfo);
		JSONArray T = jsonObject.getJSONArray("T");
		JSONArray P = jsonObject.getJSONArray("P");
		int size = T.size();
		for (int i = 0; i < size; i++) {
			JSONObject node = T.getJSONObject(i);
			int id = node.getIntValue("id");
			int x = node.getIntValue("x");
			int y = node.getIntValue("y");
			int r = node.getIntValue("r");
			TankGameInfo t = new TankGameInfo(id, x, y, r);
			tanks.add(t);
		}
		size = P.size();
		for (int i = 0; i < size; i++) {
			JSONObject node = P.getJSONObject(i);
			int tankid = node.getIntValue("S");
			int x = node.getIntValue("x");
			int y = node.getIntValue("y");
			int r = node.getIntValue("r");
			TankMapProjectile p = new TankMapProjectile(tankid, x, y, r);
			projectiles.add(p);
		}
		mPlay.updatemap(tank, tanks, projectiles);
	}

	public void tank_loop(ITtank tank) {
		while (true) {
			try {
				boolean connectflag = false;
				long nexttick = System.currentTimeMillis() + 33;
				Selector selector = Selector.open();
				long lastsendtime = 0;
				long lastrecvtime = 0;
				while (true) {
					// 选择准备好的事件
					// System.out.println("start selector.select();");
					long timeout = nexttick - System.currentTimeMillis();
					if (timeout <= 0) {
						timeout = 33;
					}
					// System.out.println(timeout);

					int channels = selector.select(timeout);

					// System.out.println("end selector.select();");
					if (tank.getState() == TankGameState.sInit) {
						tank_connect(selector);
						// send_token(tank);
						nexttick = System.currentTimeMillis() + 1000;
					} else if (tank.getState() == TankGameState.sWait) {
						nexttick = System.currentTimeMillis() + 1000;
					} else if (tank.getState() == TankGameState.sGaming) {
						long n = System.currentTimeMillis();
						if (n >= nexttick) {
							tank.clearAction();
							mPlay.gametick(tank);
							nexttick = n + 33;
							for (TankGameAction tmp : tank.getActions()) {
								String buffer = "" + (char) tmp.getAction() + "" + tmp.getArg() + "\n";
								// System.out.println(buffer);
								mSocket.write(ByteBuffer.wrap(buffer.getBytes()));
								lastsendtime = System.currentTimeMillis();

							}
							tank.clearAction();
						}
					} else {
					}

					if (connectflag == true) {
						if (System.currentTimeMillis() - lastsendtime > 10000) {
							mSocket.write(ByteBuffer.wrap("HEARTBIT\n".getBytes()));
							lastsendtime = System.currentTimeMillis();
						}
						if (lastrecvtime != 0 && System.currentTimeMillis() - lastrecvtime > 30000) {
							System.out.println("connection timedout.");
							mSocket.close();
							tank.setState(TankGameState.sInit);
							tank.setCache("");
							lastrecvtime = 0;
							connectflag = false;
							continue;
						}
					}

					if (channels == 0) {
						continue;
					}
					// 已选择的键集
					Iterator<SelectionKey> it = selector.selectedKeys().iterator();
					// 处理已选择键集事件
					while (it.hasNext()) {
						SelectionKey key = it.next();
						// 处理掉后将键移除，避免重复消费(因为下次选择后，还在已选择键集中)
						it.remove();
						if (key.isConnectable()) {

							System.out.println("Client: already connected.");

							SocketChannel client = (SocketChannel) key.channel();

							// 判断该通道是否进行连接过程、完成连接过程
							if (client.isConnectionPending()) {
								client.finishConnect();
								send_token(tank, client);
								client.register(selector, SelectionKey.OP_READ);
								lastsendtime = System.currentTimeMillis();
								connectflag = true;

							}
						} else if (key.isReadable()) {
							ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 4);
							int len = 0;
							SocketChannel client = (SocketChannel) key.channel();
							try {
								if ((len = client.read(byteBuffer)) > 0) {
									// System.out.println("接收到來自服务器的消息\n");
									String data = new String(byteBuffer.array(), 0, len);
									// System.out.println(data);
									tank.addCache(data);
									client.register(selector, SelectionKey.OP_READ);
								} else {
									// System.out.println("接收到來自服务器的消息, no data.\n");
									continue;

								}
							} catch (IOException e) {
								System.out.println("game server exception happened.........");
								key.cancel();
								mSocket.close();
								tank.setState(TankGameState.sInit);
								tank.setCache("");
								lastrecvtime = 0;
								connectflag = false;
								continue;

							}
							lastrecvtime = System.currentTimeMillis();

							// System.out.println("=========================================================");
							while (true) {
								int pos = tank.getCache().indexOf("\n");
								if (pos == -1) {
									break;
								}

								String s = tank.getCache().substring(0, pos);

								if (s.equals("HEARTBIT")) {
									tank.setCache(tank.getCache().substring(pos + 1));
									continue;
								}

								if (tank.getState() == TankGameState.sInit) {
								} else if (tank.getState() == TankGameState.sWait) {
									if (s.substring(0, 5).equals("START")) {
										tank.setState(TankGameState.sGaming);
										String[] cmdData = s.split(" ");
										log.debug("[START]" + s);
										if (cmdData.length == 2) {
											tank.setId(Integer.parseInt(cmdData[1]));
											tank.clearBlocks();
											record_start(tank, "");
											mPlay.onstart(2);
										}
										if (cmdData.length == 3) {
											tank.setId(Integer.parseInt(cmdData[1]));
											tank.clearBlocks();
											record_start(tank, cmdData[2]);
											String[] cmdBlockData = cmdData[2].split(";");
											for (String block : cmdBlockData) {
												String[] blockPos = block.split(",");
												int blockPosX = Integer.parseInt(blockPos[0]);
												int blockPosY = Integer.parseInt(blockPos[1]);
												TankMapBlock b = new TankMapBlock(blockPosX, blockPosY);
												tank.addBlock(b);
											}
											mPlay.onstart(3);

										}
									}
								} else if (tank.getState() == TankGameState.sGaming) {
									String f = s.substring(0, 1);
									if (s.equals("END")) {
										tank.setState(TankGameState.sWait);
										record_stop();

									} else if (f.equals("{")) {
										update_map(tank, s);
										record_map(s);

										// tank->updatemap(s.c_str());
									} else {
										// ...
									}
								} else {
								}

								tank.setCache(tank.getCache().substring(pos + 1));
							}
						}
					}
				}
			} catch (IOException e) {
				System.out.println(e);
				System.out.println("socket excepton happend！");
				try {
					mSocket.close();
				} catch (Exception ex) {
				}
				try {
					Thread.sleep(3 * 1000);
				} catch (Exception ex) {
				}
				tank.setState(TankGameState.sInit);
				tank.setCache("");
			}
		}
	}

}
