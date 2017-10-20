package server;
/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

import distfilesys.system.DSystem;
import distfilesys.system.DistributedFile;
import distfilesys.system.remote.DFileHandle;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import java.io.IOException;

/**
 * A UDP server
 *
 */
public final class DistFSysServer {

    public final static String default_port = "7689";
    
            
            
	private static final int PORT = Integer.parseInt(System.getProperty("port", default_port));

	public static void main(String[] args) throws Exception {
            
            DistributedFile df = new DistributedFile("config.security");
            //df.setContents("override=0");
            
            DSystem.getInstance().setDistributedFile(1, df);
     //               DFileHandle fh = new DFileHandle("timex", sys);
        
       // fh.setContents("njknin");
        //fh.store();
            

        Runnable serverTask = new Runnable() {
            @Override
            public void run() {
                DSystem.main(new String[]{});
            }
        };
        Thread serverThread = new Thread(serverTask);
        serverThread.start();
            
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			b.group(group)
			.channel(NioDatagramChannel.class)
			.option(ChannelOption.SO_BROADCAST, false)
			.handler(new UDPServerHandler());

			b.bind(PORT).sync().channel().closeFuture().await();
		} finally {
			group.shutdownGracefully();
		}
	}
}


