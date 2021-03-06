-- Create view proprietebatie based on Qgis Models

CREATE OR REPLACE VIEW #schema_cadastrapp.proprietebatie AS 
	SELECT proprietebatie.local00,
		proprietebatie.lot,
		proprietebatie.comptecommunal,
		proprietebatie.dnupro,
		proprietebatie.cgocommune,
		proprietebatie.ccopre,
		proprietebatie.ccosec,
		proprietebatie.dnupla,
		proprietebatie.jdatat,
		proprietebatie.voie,
		proprietebatie.dnvoiri,
		proprietebatie.dindic,
		proprietebatie.natvoi,
		proprietebatie.ccovoi,
		proprietebatie.libvoi,
		proprietebatie.ccoriv,
		proprietebatie.dnubat,
		proprietebatie.descr,
		proprietebatie.dniv,
		proprietebatie.dpor,
		proprietebatie.invar,
		proprietebatie.ccoaff,
		proprietebatie.ccoeva,
		proprietebatie.ccostn,
		proprietebatie.ccolloc,
		proprietebatie.gnextl,
		proprietebatie.jandeb,
		proprietebatie.janimp,
		proprietebatie.fcexb,
		proprietebatie.mvltieomx,
		proprietebatie.bateom,
		proprietebatie.jannat,
		proprietebatie.dvltrt 
	FROM dblink('host=#DBHost_qgis dbname=#DBName_qgis user=#DBUser_qgis password=#DBpasswd_qgis'::text,  
		'select 
			l.local00,
			c.lot,
			c.comptecommunal,
			c.dnupro,
			c.ccodep || c.ccodir ||	c.ccocom as cgocommune,
			l.ccopre,
			l.ccosec,
			l.dnupla,
			l.jdatat,
			v.voie,
			l.dnvoiri,
			l00.dindic,
			v.natvoi,
			l.ccovoi,
			v.libvoi,
			l00.ccoriv,
			l00.dnubat,
			l00.descr,
			l00.dniv,
			l00.dpor,
			l00.invar,
			pev.ccoaff,
			l.ccoeva,suf.ccostn,
			sufex.ccolloc,
			pevx.gnextl,
			pevx.jandeb,
			pevx.janimp,
			pevx.fcexb,
			pevtax.mvltieomx,
			pevtax.bateom,
			l.jannat,
			l.dvltrt 
		from comptecommunal c
			left join local10 as l on c.dnupro=l.dnupro
			left join local00 as l00 on l00.local00=l.local00
			left join voie as v on  l.voie=v.voie
			left join pev  on pev.local10=l.local10
			left join suf on suf.comptecommunal=c.comptecommunal and l.parcelle=suf.parcelle
			left join sufexoneration as sufex on sufex.suf=suf.suf
			left join pevexoneration as pevx on pevx.pev=pev.pev
			left join pevtaxation as pevtax on pevtax.pev=pev.pev
		order by c.ccodep,c.ccodir,c.ccocom,dnupla,v.voie,v.libvoi,l00.dnubat,l00.descr,l00.dniv,l00.dpor'::text) 
	proprietebatie(
		local00 character varying(14), 
		lot character varying, 
		comptecommunal character varying(15), 
		dnupro character varying(6), 
		cgocommune character varying(6), 
		ccopre character varying(3), 
		ccosec character varying(2), 
		dnupla character varying(4), 
		jdatat date, 
		voie character varying(19), 
		dnvoiri character varying(4), 
		dindic character varying(1), 
		natvoi character varying(4), 
		ccovoi character varying(5), 
		libvoi character varying(26),
		ccoriv character varying(4), 
		dnubat character varying(2), 
		descr character varying(2), 
		dniv character varying(2), 
		dpor character varying(5), 
		invar character varying(10),
  		ccoaff character varying(1), 
  		ccoeva character varying(1), 
  		ccostn character varying(1),
  		ccolloc character varying(2), 
		gnextl character varying(2), 
		jandeb character varying(4), 
		janimp character varying(4), 
		fcexb character varying(9),
		mvltieomx integer, 
		bateom integer,
		jannat character varying(4),
		dvltrt integer);

ALTER TABLE #schema_cadastrapp.proprietebatie OWNER TO #user_cadastrapp;
