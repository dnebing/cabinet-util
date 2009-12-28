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
 * Cab file section
 * @author allenhooo at gmail.com
 * 2009-12-24
 */
class CabFile {
	/** file is read-only */
	static final int RDONLY = (0x01);
	/** file is hidden */
	static final int HIDDEN = (0x02);
	/** file is a system file */
	static final int SYSTEM = (0x04);
	/** file modified since last backup */
	static final int ARCH = (0x20);
	/** run after extraction */
	static final int EXEC = (0x40);
	/** szName[] contains UTF */
	static final int NAME_IS_UTF = (0x80);

	/** uncompressed size of this file in bytes , 4bytes*/
	int cbFile;
	/** uncompressed offset of this file in the folder , 4bytes*/
	int uoffFolderStart;
	/** index into the CFFOLDER area , 2bytes*/
	int iFolder;
	/** date stamp for this file , 2bytes*/
	int date;
	/** time stamp for this file , 2bytes*/
	int time;
	/** attribute flags for this file , 2bytes*/
	int attribs;
	/** name of this file , 1*n bytes*/
	String szName;

	boolean isReadonly() {
		return (attribs & RDONLY) == RDONLY;
	}

	boolean isHidden() {
		return (attribs & HIDDEN) == HIDDEN;
	}

	boolean isSystem() {
		return (attribs & SYSTEM) == SYSTEM;
	}

	boolean isArch() {
		return (attribs & ARCH) == ARCH;
	}

	boolean isExec() {
		return (attribs & EXEC) == EXEC;
	}

	boolean isNameUnicode() {
		return (attribs & NAME_IS_UTF) == NAME_IS_UTF;
	}

	@Override
	public String toString() {
		return szName;
	}
}
