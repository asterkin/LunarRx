package com.cisco.vss.rx.lunar.rx.groovy.examples
import com.cisco.vss.lunar.rx.groovy.mpeg.*
import com.cisco.vss.lunar.rx.plugin.core.Lunar


class LunarMpeg2PatPlugin {

	static main(args) {
		final HOST         = args[0]
		final PORT         = Integer.parseInt(args[1])
		final DEVELOPER_ID = "6871c4b35301671668ebf26ae46b6441"
		final lunar        = new Lunar(HOST, PORT, DEVELOPER_ID)
		final SOURCE_ID    = 1
		final INPUT_PLUGIN = "source_stream"
		final INPUT_TRACK  = "stream"
		final ts           = lunar.getInputTrackStream(SOURCE_ID, INPUT_PLUGIN, INPUT_TRACK)
		
		ts
		.map({byte[] buf -> return new TsPacket(buf)})
		.filter({TsPacket tsp -> return !tsp.hasError()})
		.filter({TsPacket tsp -> return tsp.hasPayloadStart()})
		.filter({TsPacket tsp -> return (0 == tsp.pid())})
		.map({TsPacket tsp -> new PsiPat(tsp)})
		.distinctUntilChanged()
		//TODO: write Pat back to Lunar?
		.subscribe(
			{PsiPat pat -> 
			  println("New version of PAT")
			  pat.asObservable().subscribe(
				  {PsiPat.Entry entry -> 
					  println(String.format("0x%04X -> 0x%04X",entry.programNumber, entry.pid))
				  }
			  )
			}
			,{Throwable err -> println(err)}//TODO: better diagnostics?
		   ,{ -> println("Unexpected EOF")}
		)
	
	}
}
