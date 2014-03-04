package com.cisco.vss.lunar.rx.groovy.mpeg
import rx.Observable;
import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class PsiPat {
	@EqualsAndHashCode
	public class Entry {
		public Entry(final Short programNumber, final Short pid) {
			this.programNumber = programNumber
			this.pid           = pid
		}
		
		public Short programNumber
		public Short pid
	}
	
	private Entry[] table
	
	public PsiPat(final TsPacket tsp) {
		table = generatePat(tsp.payload())
	}
	
	public Observable<Entry> asObservable() {
		return Observable.from(table)
	}
	
	private Entry[] generatePat(final byte[] payload) {
		def tab = []
		for(int i=0; i < payload.length; i += 4) {
			def programNumber = makeShort(payload[i],payload[i+1])
			if(-1 == programNumber) break
			def pid = makeShort(payload[i+2], payload[i+3])
			tab.push(new Entry(programNumber, pid))
		}
		return tab
	}
	
	private Short makeShort(final byte b1, final byte b2) {
		return (Short)((b1 << 8) | b2)
	}
}
