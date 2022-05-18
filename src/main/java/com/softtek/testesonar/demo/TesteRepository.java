package com.softtek.testesonar.demo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TesteRepository {
	
	@Autowired
	HibernateTemplate hibernateTemplate;

	public List<GraficoContratoPedidosDTO> contratosUltimos30Dias(String nomeProduto) {
		StringBuilder sql = new StringBuilder();
		
		Map<String, String> parameters = new HashMap<String, String>();
		
		if(Objects.isNull(nomeProduto) || "Contrato Master de Transporte".equals(nomeProduto)) {
			sql.append("SELECT CARR.CARR_TX_NOME nomeCarregador, 'Contrato Master' as numeroPedido, sicc.SICC_DT_INCLUSAO dataInclusao, NULL nomeProduto, 'CMT' tipoPedido");
			sql.append(" FROM CONTRATO_CARREGADOR COCA");
			sql.append(" JOIN HABILITACAO_CADASTRO_CARREGADOR HACC ON COCA.HACC_CD_ID = HACC.HACC_CD_ID");
			sql.append(" JOIN CARREGADOR CARR ON HACC.CARR_CD_ID = CARR.CARR_CD_ID");
			sql.append(" JOIN CARREGADOR CARR_P ON CARR.CARR_CD_ID_PRINCIPAL = CARR_P.CARR_CD_ID");
			sql.append(" LEFT JOIN SITUACAO_CONTRATO_CARREGADOR SICC ON COCA.COCA_CD_ID = SICC.COCA_CD_ID");
			sql.append(" LEFT JOIN SITUACAO SITU ON SICC.SITU_CD_ID = SITU.SITU_CD_ID");
			sql.append(" WHERE COCA.IN_STATUS = 'A' and HACC.IN_STATUS = 'A' AND SYSDATETIME() >= COCA.COCA_DT_INICIO  AND (COCA.COCA_DT_FIM IS NULL OR SYSDATETIME() <= COCA.COCA_DT_FIM) AND SICC.SITU_CD_ID = 7 AND SICC.SICC_DT_INCLUSAO >= '" + LocalDate.now().minusYears(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "'");
		}
		
		if(Objects.isNull(nomeProduto) || !"Contrato Master de Transporte".equals(nomeProduto)) {
			
			if(Objects.isNull(nomeProduto)) {
				sql.append(" UNION ALL ");
			}
			
			//Pedidos
			sql.append(" SELECT VWPEDI.CARR_TX_NOME AS nomeCarregador, VWPEDI.PEDI_NR_PEDIDO AS numeroPedido,");
			sql.append(" VWPEDI.PEDI_DT_INCLUSAO AS dataInclusao, VWPEDI.PROD_TX_NOME AS nomeProduto,");
			sql.append(" VWPEDI.PEDI_TX_TIPO AS tipoPedido");
			sql.append(" FROM CONTRATO_PEDIDO COPE JOIN VW_PEDIDO VWPEDI ON VWPEDI.PEDI_CD_ID = COPE.PEDI_CD_ID");
			sql.append(" WHERE VWPEDI.PEDI_DT_INCLUSAO >= '"+ LocalDateTime.now().minusDays(30).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) +"'");
			
			if (Objects.nonNull(nomeProduto)) {
				if (nomeProduto.equalsIgnoreCase("Longo Prazo")) {				
					sql.append(" AND VWPEDI.PEDI_TX_TIPO LIKE '%LEILAO%'");
				} else {
					
					sql.append(" AND VWPEDI.PROD_TX_NOME LIKE '%" + nomeProduto +  "%'");
				}
			}
		}
		
		sql.append(" ORDER BY 2 DESC");
		
		
		
		return createNativeQuery(sql.toString(), parameters,GraficoContratoPedidosDTO.class);
	}
	
	@SuppressWarnings({ "deprecation", "rawtypes" })
	private List createNativeQuery(String sql, Map<String, String> parameters, Class target) {
		Query query = getCurrentSession().createNativeQuery(sql);
		if (!parameters.isEmpty()) {
			for (Entry<String, String> parameter : parameters.entrySet()) {
				query.setParameter(parameter.getKey(), parameter.getValue());					
			}	
		}
		return query.unwrap(org.hibernate.query.NativeQuery.class)
				.setResultTransformer(Transformers.aliasToBean(target)).getResultList();
	}
	
	protected Session getCurrentSession() {
		SessionFactory sessionFactory = hibernateTemplate.getSessionFactory();
		return sessionFactory.getCurrentSession();
	}
	
}
