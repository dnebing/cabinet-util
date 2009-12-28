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

/**
 * Cab header section.<br>
 * Cab file format please refer to Microsoft Cabinet File Format
 * @author allenhooo at gmail.com
 * 2009-12-24
 */
class CabHeader {
	/** FLAG_PREV_CABINET 
	 * is set if this cabinet file is not the first in a set of cabinet files.  
	 * When this bit is set, the szCabinetPrev and szDiskPrev fields are present in this CFHEADER. */
	static final int FLAG_PREV_CABINET = 0x0001;
	/**
	 *FLAG_NEXT_CABINET is set if this cabinet file is not the last in a set of cabinet files.  
	 *When this bit is set, the szCabinetNext and szDiskNext fields are present in this CFHEADER.
	 */
	static final int FLAG_NEXT_CABINET = 0x0002;
	/**
	 *  FLAG_RESERVE_PRESENT is set if this cabinet file contains any reserved fields.  
	 *  When this bit is set, the cbCFHeader, cbCFFolder, and cbCFData fields are present in this CFHEADER.
	 */
	static final int FLAG_RESERVE_PRESENT = 0x0004;

	/** cabinet file signature , 4*1 bytes*/
	char[] signature = new char[4];
	/** reserved , 4 bytes*/
	int reserved1;
	/** size of this cabinet file in bytes , 4 bytes*/
	int cbCabinet;
	/** reserved , 4 bytes*/
	int reserved2;
	/** offset of the first CFFILE entry , 4 bytes*/
	int coffFiles;
	/** reserved , 4 bytes*/
	int reserved3;
	/** cabinet file format version, minor , 1 bytes*/
	short versionMinor;
	/** 1 cabinet file format version, major , 1 bytes*/
	short versionMajor;
	/** 2 number of CFFOLDER entries in this cabinet , 2 bytes*/
	int cFolders;
	/** 2 number of CFFILE entries in this cabinet , 2 bytes*/
	int cFiles;
	/** 2 cabinet file option indicators , 2 bytes*/
	int flags;
	/** 2 must be the same for all cabinets in a set , 2 bytes*/
	int setID;
	/** 2 number of this cabinet file in a set , 2 bytes*/
	int iCabinet;
	/** 2 (optional) size of per-cabinet reserved area , 2 bytes*/
	int cbCFHeader;
	/** 1 (optional) size of per-folder reserved area , 1 bytes*/
	short cbCFFolder;
	/** 1 (optional) size of per-datablock reserved area , 1 bytes*/
	short cbCFData;
	/** (optional) per-cabinet reserved area , 1*n bytes*/
	short abReserve[];
	/** (optional) name of previous cabinet file , 1*n bytes*/
	short szCabinetPrev[];
	/** (optional) name of previous disk , 1*n bytes*/
	short szDiskPrev[];
	/** (optional) name of next cabinet file , 1*n bytes*/
	short szCabinetNext[];
	/** (optional) name of next disk , 1*n bytes*/
	short szDiskNext[];

	boolean prevCabinet() {
		return (flags & FLAG_PREV_CABINET) == FLAG_PREV_CABINET;
	}

	boolean nextCabinet() {
		return (flags & FLAG_NEXT_CABINET) == FLAG_NEXT_CABINET;
	}

	boolean reservePresent() {
		return (flags & FLAG_RESERVE_PRESENT) == FLAG_RESERVE_PRESENT;
	}
}

