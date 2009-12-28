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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * Reading Microsoft cabinet file format
 * <p>
 * please refer Microsoft Cabinet File Format。 Cabinet files support a number of compression formats, we support only MSZIP and uncompressed.<p>
 * <b>WARNING:</b>Checksums are not supported。
 * @author allenhooo at gmail.com 2009-12-25
 */
public class CabinetReader {
	/** speed or memory */
	private boolean speed_first = false;

	private CabHeader head = new CabHeader();
	private CabFolder[] folders = null;
	private CabFile[] files = null;

	private RandomAccessFile file = null;

	private String[] filename;

	private int offset = 0;

	/**
	 * Create a cabinet reader with the filename.
	 * @param filename cab file name
	 * @throws IOException
	 */
	public CabinetReader(String filename) throws IOException {
		this(new File(filename));
	}

	/**
	 * Create a cabinet reader with the File object.
	 * @param f  cab file
	 * @throws IOException
	 */
	public CabinetReader(File f) throws IOException {
		file = new RandomAccessFile(f, "r");
		readHeader();
		readCabFolder();
		readCabFile();
		readData();
	}

	public void close() throws IOException {
		file.close();
	}

	/**
	 * Fetch file name which packed in the cab file。
	 * @return
	 */
	public String[] getFileNames() {
		if (filename == null) {
			filename = new String[files.length];
			for (int i = 0; i < files.length; i++) {
				filename[i] = files[i].szName;
			}
		}
		return filename;
	}

	/**
	 * Create a InputStream with the packed file name.<p>
	 * The file name can be fetched with {@link #getFileNames()} method
	 * @param name 
	 * @return 
	 * @throws IOException
	 */
	public InputStream getFileInputStream(String name) throws IOException {
		int i = 0;
		for (; i < files.length; i++) {
			if (name.equals(files[i].szName)){
				return readFile(files[i]);
			}
		}
		throw new IOException("Invalid file entry");
	}

	private void readHeader() throws IOException {
		head.signature[0] = (char) file.read();
		head.signature[1] = (char) file.read();
		head.signature[2] = (char) file.read();
		head.signature[3] = (char) file.read();
		offset += 4;

		String s = new String(head.signature);
		if (!"MSCF".equals(s)) {
			throw new IOException("Invalid CAB file");
		}

		head.reserved1 = (int) readNum(4);
		head.cbCabinet = (int) readNum(4);
		head.reserved2 = (int) readNum(4);
		head.coffFiles = (int) readNum(4);
		head.reserved3 = (int) readNum(4);
		head.versionMinor = (short) readNum(1);
		head.versionMajor = (short) readNum(1);
		head.cFolders = (int) readNum(2);
		head.cFiles = (int) readNum(2);
		head.flags = (int) readNum(2);
		head.setID = (int) readNum(2);
		head.iCabinet = (int) readNum(2);

		if (head.reservePresent()) {
			head.cbCFHeader = (int) readNum(2);
			head.cbCFFolder = (short) readNum(1);
			head.cbCFData = (short) readNum(1);
		}

		head.abReserve = new short[head.cbCFHeader];
		for (int i = 0; i < head.cbCFHeader; i++) {
			head.abReserve[i] = (short) readNum(1);
		}

		if (head.prevCabinet()) {
			// TODO
			head.szCabinetPrev = new short[0];
			head.szDiskPrev = new short[0];
		}

		if (head.nextCabinet()) {
			// TODO
			head.szCabinetNext = new short[0];
			head.szDiskNext = new short[0];
		}
	}

	private void readCabFolder() throws IOException {
		folders = new CabFolder[head.cFolders];
		for (int i = 0; i < head.cFolders; i++) {
			folders[i] = new CabFolder();
			folders[i].coffCabStart = (int) readNum(4);
			folders[i].cCFData = (int) readNum(2);
			folders[i].typeCompress = (int) readNum(2);
			if (head.reservePresent() && head.cbCFFolder != 0) {
				folders[i].abReserve = new short[head.cbCFFolder];
				for (int j = 0; j < head.cbCFFolder; j++) {
					folders[i].abReserve[j] = (short) readNum(1);
				}
			}
		}
	}

	private void readCabFile() throws IOException {
		files = new CabFile[head.cFiles];
		if (head.coffFiles > offset) {
			skip(head.coffFiles - offset);
		}
		for (int i = 0; i < files.length; i++) {
			files[i] = new CabFile();
			files[i].cbFile = (int) readNum(4);
			files[i].uoffFolderStart = (int) readNum(4);
			files[i].iFolder = (int) readNum(2);
			files[i].date = (int) readNum(2);
			files[i].time = (int) readNum(2);
			files[i].attribs = (int) readNum(2);

			byte[] namebuffer = new byte[256];
			int x = 0;
			for (;;) {
				byte b = readByte();
				if (b == 0) {
					break;
				}
				namebuffer[x++] = b;
			}
			if (files[i].isNameUnicode()) {
				files[i].szName = new String(namebuffer, 0, x, "UNICODE");
			} else {
				files[i].szName = new String(namebuffer, 0, x);
			}
		}
	}

	private void readData() throws IOException {
		for (int i = 0; i < folders.length; i++) {
			folders[i].cabData = new CabData[folders[i].cCFData];
			for (int j = 0; j < folders[i].cabData.length; j++) {
				CabData cabData = new CabData(file, isSpeed_first());
				cabData.csum = (int) readNum(4);
				cabData.cbData = (int) readNum(2);
				cabData.cbUncomp = (int) readNum(2);
				if (head.reservePresent()) {
					cabData.abReserve = new short[head.cbCFData];
				}
				// 延迟读取
				cabData.offset = offset;
				skip(cabData.cbData);
				folders[i].cabData[j] = cabData;
			}
		}
	}

	private InputStream readFile(CabFile file) throws IOException {
		CabFolder folder = folders[file.iFolder];
		return new CabinetInputStream(folder, file);
	}

	private void skip(int bytenum) throws IOException {
		file.skipBytes(bytenum);
		offset += bytenum;
	}

	/**
	 * 读取bytenum个byte，组成一个long, cab包所有数据采用little-ending ,
	 * */
	private long readNum(int bytenum) throws IOException {
		int result = 0;
		for (int i = 0; i < bytenum; i++) {
			int a = file.read();
			result = result + (a << (i * 8));
		}
		offset += bytenum;
		return result;
	}

	private byte readByte() throws IOException {
		offset++;
		return (byte) file.read();
	}

	public void setSpeed_first(boolean speed_first) {
		this.speed_first = speed_first;
	}

	public boolean isSpeed_first() {
		return speed_first;
	}
}
