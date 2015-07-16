package org.georchestra.cadastrapp.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;


public class InfoBulleController extends CadController {
	
	final static Logger logger = LoggerFactory.getLogger(InfoBulleController.class);


	@Path("/getInfoBulle")
	@GET
	@Produces("application/json")
	/**
	 * 
	 * @param headers / headers from request used to filter search using LDAP Roles to display only information about parcelle from available ccoinsee
	 * 
	 * @param parcelle id parcelle
	 * @param infocadastrale 1 to get additional information, 0 to avoid getting additional information (default value 1 if not set)
	 * @param infouf 1 to get additional information, 0 to avoid getting additional information (default value 1 if not set)
	 * 
	 * @return Data from parcelle view to be display in popup in JSON format
	 * 
	 * @throws SQLException
	 */
	public Map<String, Object> getInfoBulle(
			@Context HttpHeaders headers,
			@QueryParam("parcelle") String parcelle,
			@DefaultValue("1") @QueryParam("infocadastrale") int infocadastrale,
			@DefaultValue("1") @QueryParam("infouf") int infouf) throws SQLException {
 
		Map<String, Object> informations = null;
		
		//TODO simplify this
		if(infocadastrale == 0 && infouf == 1){
			informations = getInfoBulleUniteFonciere(headers, parcelle);
		}else if (infocadastrale == 1 && infouf == 0){
			informations = getInfoBulleParcelle(headers, parcelle);
		}else if (infocadastrale == 0 && infouf == 0){
			logger.warn("No information can be serve");
		}else{
			informations = getInfoBulleParcelle(headers, parcelle);
			informations.putAll(getInfoBulleUniteFonciere(headers, parcelle));
		}

		return informations;
	}

	
	@Path("/getInfobulleParcelle")
	@GET
	@Produces("application/json")
	/**
	 * 
	 * @param headers / headers from request used to filter search using LDAP Roles to display only information about parcelle from available ccoinsee
	 * 
	 * @param parcelle id parcelle
	 * @return Data from parcelle view to be display in popup in JSON format
	 * 
	 * @throws SQLException
	 */
	public Map<String, Object> getInfoBulleParcelle(
			@Context HttpHeaders headers,
			@QueryParam("parcelle") String parcelle) throws SQLException {
 
		Map<String, Object> informations = null;

		if (isMandatoryParameterValid(parcelle)){
		
			// Create query
			StringBuilder queryBuilder = new StringBuilder();
	
			queryBuilder.append("select p.parcelle, p.surfc, c.libcom, p.dcntpa, p.dnvoiri, p.dindic, p.cconvo, p.dvoilib from ");
			queryBuilder.append(databaseSchema);
			queryBuilder.append(".parcelle p,");
			queryBuilder.append(databaseSchema);
			queryBuilder.append(".commune c where p.parcelle = ? and p.ccocom = c.ccocom and p.ccodep = c.ccodep");

						
			JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
			informations = jdbcTemplate.queryForMap(queryBuilder.toString(), parcelle);
			
			if(getUserCNILLevel(headers)>0){
				
				List<Map<String, Object>> proprietaires = null;
				
				// Create query
				//TODO change this to use view proprietaire_parcelle
				StringBuilder queryProprietaireBuilder = new StringBuilder();
				queryProprietaireBuilder.append("select prop.ddenom from ");
				queryProprietaireBuilder.append(databaseSchema);
				queryProprietaireBuilder.append(".proprietaire_parcelle proparc,");
				queryProprietaireBuilder.append(databaseSchema);
				queryProprietaireBuilder.append(".proprietaire prop ");
				queryProprietaireBuilder.append(" where proparc.parcelle = ? and proparc.comptecommunal = prop.comptecommunal LIMIT 5");	
				
				JdbcTemplate jdbcTemplateProp = new JdbcTemplate(dataSource);
				proprietaires = jdbcTemplateProp.queryForList(queryProprietaireBuilder.toString(), parcelle);
				
				// Add a new node proprietaire, fill with ddenom List
				informations.put("proprietaires", proprietaires);
				
				logger.debug("List des informations avec proprietaire : "+ informations.toString());
			}
		}
		else{
			logger.warn("missing mandatory parameters");
		}

		return informations;
	}

	@Path("/getInfobulleUniteFonciere")
	@GET
	@Produces("application/json")
	/**
	 * 
	 * @param headers / headers from request used to filter search using LDAP Roles to display only information about parcelle from available ccoinsee
	 * 
	 * @param parcelle id parcelle
	 * @return Data from parcelle view to be display in popup in JSON format
	 * 
	 * @throws SQLException
	 */
	public Map<String, Object> getInfoBulleUniteFonciere(
			@Context HttpHeaders headers,
			@QueryParam("parcelle") String parcelle) throws SQLException {
 
		Map<String, Object> informations = null;

		if (isMandatoryParameterValid(parcelle)){
		
			// Create query
			StringBuilder queryBuilder = new StringBuilder();
		
			//TODO add batical
			queryBuilder.append("select p.comptecommunal, sum(p.dcntpa) as dcntpa_sum, sum(p.surfc) as sigcal_sum from ");
			queryBuilder.append(databaseSchema);
			queryBuilder.append(".parcelle p, ");
			queryBuilder.append(databaseSchema);
			queryBuilder.append(".proprietaire_parcelle proparc where proparc.parcelle = ? and proparc.comptecommunal = p.comptecommunal GROUP BY p.comptecommunal;");
						
			JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
			informations = jdbcTemplate.queryForMap(queryBuilder.toString(), parcelle);
		}
		else{
			logger.warn("missing mandatory parameters");
		}

		return informations;
	}

}
