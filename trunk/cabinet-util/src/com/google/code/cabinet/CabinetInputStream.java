/*
  Copyright [2009] [allenhooo at gmail dot com]

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.google.code.cabinet;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class implements an input stream for reading files in the CAB file format.
 * Includes support for both MSZIP compressed and uncompressed entries。<p>
 * 
 * @author allenhooo at gmail.com 2009-12-24
 */
class CabinetInputStream extends InputStream {

	/** 文件所在的folder */
	private final CabFolder folder;
	/** 文件 */
	private final CabFile file;

	/**当前文件读取指针*/
	private int pos = 0;
	/**当前文件指针pos所在数据数据块*/
	private int blockIndex = 0;
	/**当前文件指针pos所在的数据块的数据byte*/
	private int dataIndex = 0;
	
	/**当前解压后的数据，只保存当前指针所在的block解压后数据*/
	private byte[] currentUnCompData;
	/**当前解压数据的block索引*/
	private int uncompBlock=0;
	
	private boolean closed = false;

	private void ensureOpen() throws IOException{
		if(closed){
			 throw new IOException("Stream closed");
		}
	}
	
	public CabinetInputStream(CabFolder folder, CabFile file) {
		this.folder = folder;
		this.file = file;

		//初始化cab读取流，确定文件所在的起始位置指针
		int offstart = file.uoffFolderStart;
		for (int i = 0; i < folder.cCFData; i++) {
			int ablen = folder.cabData[i].cbUncomp;

			if (pos + ablen < offstart) {
				// 加上整个block也不能达到offstart
				pos += ablen;
				blockIndex++;
				continue;
			} else {
				int diff = offstart - pos;
				pos += diff;
				dataIndex += diff;
				break;
			}
		}
	}

	@Override
	public int read() throws IOException {
		ensureOpen();
		byte[] buffer = new byte[1];
		int read = read(buffer);
		
		if(read <1){
			return -1;
		}
		int data = buffer[0];
		if (data < 0) {
			data = 256 + data;
		}
		return data;
	}

	private boolean EOF() {
		return ((pos - file.uoffFolderStart > file.cbFile)
				|| blockIndex >= folder.cCFData);
	}

	private byte[] getCurrentBlock() throws IOException {
		if (blockIndex >= folder.cCFData) {
			return null;
		}
		if (uncompBlock != blockIndex || currentUnCompData == null) {
			currentUnCompData = folder.getUnCompressedData(blockIndex);
			uncompBlock = blockIndex;
		}
		return currentUnCompData;
	}

	private int getCurrentBlockSize() {
		return folder.cabData[blockIndex].cbUncomp;
	}

	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	public int read(byte[] b, int off, int len) throws IOException {
		return read(b, off, len, false);
	}
	
	public int read(byte[] b, int off, int len, boolean skip) throws IOException {
		ensureOpen();
		 if(len == 0) {
			    return 0;
		}
		if (len > b.length - off) {
			throw new IOException("Parameter error");
		}
		if (EOF()) {
			return -1;
		}
		int read_len = 0;
		for (int i = blockIndex; i < folder.cCFData; i++) {
			if (EOF()) {
				break;
			}
			if (len <= read_len) {
				//读取完成
				break;
			}

			//计算当前块剩余字节数量
			int block_remain_len = getCurrentBlockSize() - dataIndex;
			if (block_remain_len <= 0) {
				//无剩余数据，跳转到下一个 block
				blockIndex++;
				dataIndex = 0;
				continue;
			}
			//计算文件剩余字节数
			int file_remain = file.cbFile- (pos - file.uoffFolderStart);
			if(file_remain <= 0){
				if(read_len == 0){
					return -1;
				}else{
					//前面有读取过数据
					return read_len;
				}
			}
			int to_read = len - read_len;//余下需要读取的字节数
			byte[] block = null;
			if(!skip){
				block = getCurrentBlock();
			}
			if (to_read <= block_remain_len) {
				//取文件剩余字节数和需要读取字节数的最小值
				to_read = Math.min(file_remain, to_read);
				if(!skip){
					System.arraycopy(block, dataIndex, b, read_len, to_read);
				}
				read_len += to_read;
				dataIndex+= to_read;
				pos+=to_read;
			} else {
				//取文件剩余字节数和当前块剩余字节数的最小值
				block_remain_len = Math.min(file_remain, block_remain_len);
				if(!skip){
					System.arraycopy(block, dataIndex, b, read_len,block_remain_len);
				}
				blockIndex++;
				dataIndex = 0;
				read_len += block_remain_len;
				pos+=block_remain_len;
			}
		}
		
		return read_len;
	}

	@Override
	public long skip(long n) throws IOException {
		if (n < 0) {
            throw new IllegalArgumentException("negative skip length");
        }
		if(n>Integer.MAX_VALUE){
			throw new IOException("不支持long");
		}
		return read(null, 0, (int)n, true);
	}

	@Override
	public int available() throws IOException {
		return file.cbFile;
	}
	@Override
	public void close() throws IOException {
		closed = true;
		super.close();
	}
}
