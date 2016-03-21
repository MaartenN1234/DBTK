package mn.dbtk.sql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mn.dbtk.sql.ParseSQLHelper.ExpressionAliasPair;
import mn.dbtk.util.MultiSet;


public class ParsedSQLStatement {
	public List<TableEntry>     tableList;
	public List<SelectEntry>    selectList;
	public List<String>         whereClauses;
	public List<String>         whereClausesNonOJ;
	public List<String>         filterClauses;
	
	Set<String> uniqueSelectAliasses;
	Set<String> uniqueTableAliasses;
	MultiSet<String, String> columnTableMap;
	
	
	public boolean      parseOk;
	public List<String> parseWarnings;
	public List<String> parseErrors;
			
	public ParsedSQLStatement(String selectText, String fromText, String whereText, String additionalFilterText){
		parseWarnings = new ArrayList<String>();
		parseErrors   = new ArrayList<String>();
		
		if(!DBObjectCache.cache.isLoaded(true)){
			parseErrors.add("Cache could not be loaded. "+DBObjectCache.cache.lastException);
			return;
		}
		
		tableList     = parseFrom(fromText);
		selectList    = parseSelect(selectText);
		whereClauses  = parseWhere(whereText);
		filterClauses = parseFilter(additionalFilterText);
		
		if (tableList.size() == 0)
			parseErrors.add("No tables found in FROM list");
		if (selectList.size() == 0)
			parseErrors.add("No selection found in SELECT list");
		
		parseOk       = parseErrors.size()==0;
	}
	

	
	private List<TableEntry> parseFrom(String fromText) {
		List<TableEntry> result = new ArrayList<TableEntry>();
		uniqueTableAliasses     = new HashSet<String>();
		columnTableMap          = new MultiSet<String, String>();
		
		List<String> blocks  = ParseSQLHelper.sqlParseBlocks(fromText, true);
		List<String> entrees = ParseSQLHelper.blocksToListEntrees(blocks, ",");
		
		for (String entry : entrees){
			boolean outerjoin = false;			
			if (entry.endsWith("+")){
				outerjoin = true;
				entry = entry.substring(0,entry.length()-1).trim();
			} else if (entry.endsWith("(+)")){
				outerjoin = true;
				entry = entry.substring(0,entry.length()-3).trim();				
			}
			
			ExpressionAliasPair pair =  ParseSQLHelper.findAlias(entry);
			uniqueTableAliasses.add(pair.alias);
			
			// Validation and maintaining column counts
			DBObjectsModelRowSource rs = DBObjectCache.cache.getCache(pair.expression);
						
			result.add(new TableEntry(pair, outerjoin, rs));
		
			
			if (rs == null){
				if (pair.expression.startsWith("(")){
					parseWarnings.add("Could not resolve source table for \'"+pair.alias+"\' as it's an inline view");
				} else {
					parseErrors.add("Rowsource not found \'"+pair.expression+"\'");
				}
			} else {
				for (DBObjectsModelColumn column : rs.columns){
					columnTableMap.add(column.name, pair.alias);
				}
			}
		}
	
		return result;
	}
	
	private List<SelectEntry> parseSelect(String selectText) {
		List<SelectEntry> result   = new ArrayList<SelectEntry>();
		uniqueSelectAliasses = new HashSet<String>();
		
		List<String> blocks  = ParseSQLHelper.sqlParseBlocks(selectText, true);
		List<String> entrees = ParseSQLHelper.blocksToListEntrees(blocks, ",");
		boolean      hasAnyAggregate = false;
		
		for (String entry : entrees){
			ExpressionAliasPair pair =  ParseSQLHelper.findAlias(entry);
			List<ExpressionAliasPair> pairs = new ArrayList<ExpressionAliasPair>();
			
			// * replacement
			if (pair.alias.equals("*")){
				if (pair.expression.equals("*")){
					for (TableEntry table : tableList)
						pairs.addAll(getSelectStarForTableAlias(table.alias));
				} else {
					int lio = pair.expression.lastIndexOf(".");
					int fio = pair.expression.lastIndexOf(".",lio-1);
					if (fio <0 ) 
						fio=0;
					String tableAlias = pair.expression.substring(fio,lio);
					pairs.addAll(getSelectStarForTableAlias(tableAlias));					
				}
			} else {
				pairs.add(pair);
			}

			for (ExpressionAliasPair singlepair : pairs){
				singlepair.alias = getUniqueAlias(singlepair.alias);
				
				boolean hasAggregate = false;
				
				for (String func: DBObjectCache.cache.aggregateFunctions)
					hasAggregate |= ParseSQLHelper.expressionContainsVariable(singlepair.expression, func, false, false);
				
				result.add(new SelectEntry(singlepair, !hasAggregate));
				hasAnyAggregate |= hasAggregate;				
			}			
		}
		
		if (!hasAnyAggregate){
			for (SelectEntry se : result)
				se.groupBy = false;
		}
		
	
		return result;
	}	
	


	private List<String> parseWhere(String whereText) {
		List<String> blocks  = ParseSQLHelper.sqlParseBlocks(whereText, true);
		List<String> entrees = ParseSQLHelper.blocksToListEntrees(blocks, "AND");
		List<String> result  = new ArrayList<String>();
		for(String entry : entrees)
			result.add(entry.trim());
		
		return result;
	}
	
	private List<String> parseFilter(String additionalFilterText) {
		List<String> blocks  = ParseSQLHelper.sqlParseBlocks(additionalFilterText, true);
		List<String> entrees = ParseSQLHelper.blocksToListEntrees(blocks, "AND");
		List<String> result  = new ArrayList<String>();
		for(String entry : entrees)
			result.add(entry.trim());
		
		return result;
	}
	

	private String getUniqueAlias(String alias) {
		return getUniqueAlias(alias, false);
	}
	private String getUniqueAlias(String alias, boolean isTableAliasList) {
		Set<String> uniqueAliasses =isTableAliasList ? uniqueTableAliasses : uniqueSelectAliasses;
		boolean qouted         = alias.startsWith("\"");
		
		String  strippedAlias  = qouted ? alias.substring(1,alias.length()-1): alias;
		String  suggestedAlias = strippedAlias;

		int cnt = 0;
		
		while(uniqueAliasses.contains(suggestedAlias)){
			String suffix =""+cnt;
			if (strippedAlias.length()+suffix.length() > 30)
				suggestedAlias = strippedAlias.substring(0, 29-suffix.length());
			
			suggestedAlias = strippedAlias + suffix;
			cnt++;
		}

		uniqueAliasses.add(suggestedAlias);

		return (qouted? "\"":"") +suggestedAlias + (qouted? "\"":"");
	}


	private List<ExpressionAliasPair> getSelectStarForTable(String resolvedName, String alias) {
		List<ExpressionAliasPair> result = new ArrayList<ExpressionAliasPair>();

		if (resolvedName == null){
			result.add(new ExpressionAliasPair(alias+".*", ""));
		} else {
			DBObjectsModelRowSource rs = DBObjectCache.cache.getCache(resolvedName);
			if (rs == null){
				result.add(new ExpressionAliasPair(alias+".*", ""));
			} else {
				for (DBObjectsModelColumn column : rs.columns){
					if(columnTableMap.get(column.name) != null && columnTableMap.get(column.name).size() > 1)
						result.add(new ExpressionAliasPair(alias +"." + column.name, column.name));
					else
						result.add(new ExpressionAliasPair(column.name, column.name));
				}
			}
		}
		
		return result;
	}
	private List<ExpressionAliasPair> getSelectStarForTableAlias(String aliasRaw) {
		String alias = ParseSQLHelper.cleanFormatRawAlias(aliasRaw);
		return getSelectStarForTable(resolveTableAlias(alias), alias);
	}
	
	private String resolveTableAlias(String aliasRaw){
		String alias = ParseSQLHelper.cleanFormatRawAlias(aliasRaw);
		for (TableEntry tableEntry : tableList){
			if    (tableEntry.alias.equals(alias)){
				if (tableEntry.expression.startsWith("(")){
					parseWarnings.add("Could not resolve source table for alias \'"+alias+"\' as it's an inline view");
					return null;
				}
				return tableEntry.expression;
			}
		}
		parseErrors.add("Could not resolve source table for alias \'"+alias+"\'");
		return null;
	}



	public String toString(){
		return selectList + "\r\n"
			  + tableList + "\r\n"
			  + whereClauses + "\r\n"
			  + filterClauses;
	}	
	
	public String toSQLSelect(){
		StringBuilder sb = new StringBuilder();
		boolean first;

		first = true;
		for (SelectEntry se : selectList){
			if (!first)
				sb.append(", ");
			sb.append(se.toSQL());
			first = false;
		}
		
		return sb.toString();
	}
	public String toSQLFrom(boolean forDisplay){
		StringBuilder sb = new StringBuilder();
		boolean first;

		first = true;
		for (TableEntry te : tableList){
			if (!first)
				sb.append(", ");
			sb.append(te.toSQL(forDisplay));
			first = false;
		}

		return sb.toString();
	}	
		

	public String toSQLWhere(List<String> clauses, int addTabs, boolean forDisplay){
		StringBuilder sb = new StringBuilder();
		String addedTabsX = "\t\t\t\t\t\t\t\t\t".substring(0,addTabs>0?addTabs-1:0);
		List<String> outerjoinVariables = getOuterjoinVariableList();
		boolean first;

		first = true;
		for (String clause : clauses){
			if (!forDisplay)
				for (String var : outerjoinVariables)
					clause = ParseSQLHelper.expressionReplaceVariable(clause, var, var +"(+)", false, false);
				
			if (!first)
				sb.append((addTabs >=0?"\r\n":"")+addedTabsX+" AND ");
			sb.append((addTabs>0?"\t":"")+clause+" ");
			first = false;
		}
		return sb.toString();
	}
	
	
	private List<String> getOuterjoinVariableList() {
		List<String> result = new ArrayList<String>();
		for (TableEntry te: tableList)
			result.addAll(getOuterjoinVariableList(te));
			
		return result;
	}
	private List<String> getOuterjoinVariableList(TableEntry te) {
		List<String> result = new ArrayList<String>();
		if (te.outerjoined){
			List<ExpressionAliasPair> eaps = getSelectStarForTable(te.expression, te.alias);
			for(ExpressionAliasPair eap : eaps){
				result.add(te.alias + "." + eap.alias);
				if (columnTableMap.get(eap.alias) != null && columnTableMap.get(eap.alias).size()==1)
					result.add(eap.alias);
			}
		}
		return result;
	}


	public String toSQL(){
		StringBuilder sb = new StringBuilder();
		boolean first;

		// Outer SELECT
		sb.append("SELECT * FROM (");
		
		// Inner SELECT
		sb.append("\r\n\tSELECT\t");
		sb.append(toSQLSelect());
		
		// Inner FROM
		sb.append("\r\n\tFROM\t");
		sb.append(toSQLFrom(false));

		// Inner WHERE
		if (whereClauses.size()> 0){
			sb.append("\r\n\tWHERE");
		}
		sb.append(toSQLWhere(whereClauses, 2, false));
		
		// Inner GROUP BY
		first = true;
		for (SelectEntry se : selectList){
			if (se.groupBy){
				if (first)
					sb.append("\r\n\tGROUP BY\t");
				else
					sb.append(", ");
					
				sb.append(se.expression);
				first = false;
			}
		}
		
		// Outer WHERE
		sb.append(")");
		if (filterClauses.size()> 0){
			sb.append("\r\nWHERE ");
		}
		sb.append(toSQLWhere(filterClauses, 1, false));

		return sb.toString();
	}
	
	
	private int getSelectColumnIndex(String columnName) {
		int i = -1;
		int j = 0;
		for (SelectEntry se : selectList){
			if (ParseSQLHelper.stripQoutes(se.alias).equals(ParseSQLHelper.stripQoutes(columnName)))
				i = j;
			j++;
		}
		return i;
	}
	private int getTableColumnIndex(String tableAlias) {
		int i = -1;
		int j = 0;
		for (TableEntry te : tableList){
			if (ParseSQLHelper.stripQoutes(te.alias).equals(ParseSQLHelper.stripQoutes(tableAlias)))
				i = j;
			j++;
		}
		return i;
	}
	
	private List<Integer> getSelectListIndexesForTableAlias(String tableAlias){
		Set<Integer> result   = new HashSet<Integer>();
		List<String>  compares = getColumnReferencesForTableAlias(tableAlias);
		for (int i=0; i<selectList.size();i++){
			String expression = selectList.get(i).expression;
			
			for(String compare : compares)
				if (ParseSQLHelper.expressionContainsVariable(expression,compare, false, false))
					result.add(i);
		}

		return new ArrayList<Integer>(result);
	}
	private List<Integer> getClauseListIndexesForTableAlias(String tableAlias){
		Set<Integer> result   = new HashSet<Integer>();
		List<String>  compares = getColumnReferencesForTableAlias(tableAlias);
		for (int i=0; i<whereClauses.size();i++){
			String expression = whereClauses.get(i);
			for(String compare:compares)
				if (ParseSQLHelper.expressionContainsVariable(expression,compare, false, false))
					result.add(i);
		}

		return new ArrayList<Integer>(result);
	}
	private List<String> getColumnReferencesForTableAlias(String tableAlias){
		List<String> result = new ArrayList<String>();
		result.add(tableAlias+".");
		for (ExpressionAliasPair pair : getSelectStarForTableAlias(tableAlias)){
			result.add(pair.alias);
			result.add(tableAlias +"."+pair.alias);
		}
		
		return result;
	}

	
	public void removeSelectColumn(String columnName) {
		removeSelectColumn(columnName, null);
	}
	private void removeSelectColumn(String columnName, String alternative) {
		int index = getSelectColumnIndex(columnName);
		
		if (index !=- 1){
			List<String> newFilterClauses = new ArrayList<String>();
			for (String clause : filterClauses){
				if (!clause.contains(columnName)){
					newFilterClauses.add(clause);
				} else if(alternative != null){
					newFilterClauses.add(clause.replace(columnName, alternative));
				}
			}
			filterClauses = newFilterClauses;
		
			selectList.remove(index);
		}		
	}
	
	public void changeAliasInSelectList(String oldValue, String newValue) {
		int index = getSelectColumnIndex(oldValue);
		
		if (index !=- 1){
			List<String> newFilterClauses = new ArrayList<String>();
			for (String clause : filterClauses){
				newFilterClauses.add(clause.replace(oldValue, newValue));
			}
			filterClauses = newFilterClauses;
		
			selectList.get(index).alias = newValue;
		}		
		
	}	
	public void addToSelectList(String sourceTableAlias, String columnName) {
		String expression = 
				((columnTableMap.get(columnName) == null || columnTableMap.get(columnName).size() > 1) ?
						sourceTableAlias + "." : "") +
				columnName;
		String alias = getUniqueAlias(columnName);
		boolean groupBy = false;
		
		for (SelectEntry se : selectList)
			groupBy |= se.groupBy;
		
		selectList.add(new SelectEntry(expression, alias, groupBy));		
	}
	public void addToTableList(String addedTable) {
		String expression = addedTable;
		String alias = getUniqueAlias(expression, true);
		
		for(ExpressionAliasPair eap : getSelectStarForTable(addedTable, alias)){
			if(columnTableMap.get(eap.alias) != null && columnTableMap.get(eap.alias).size()==1){
				String aliasForOldTable = columnTableMap.get(eap.alias).iterator().next();
				
				for(SelectEntry se : selectList){
					se.expression = ParseSQLHelper.expressionReplaceVariable(se.expression, eap.alias, aliasForOldTable + "." + eap.alias, false, false);
				}
				List<String> newWhereClauses = new ArrayList<String>();
				for(String whereClause : whereClauses){
					String newWhereClause = ParseSQLHelper.expressionReplaceVariable(whereClause, eap.alias, aliasForOldTable + "." + eap.alias, false, false);
					newWhereClauses.add(newWhereClause);
				}
				whereClauses = newWhereClauses;
				
			}			
			columnTableMap.add(eap.alias, alias);
		}
		
		
		tableList.add(new TableEntry(expression, alias, false));
	}
	public boolean isTableUsed(String tableAlias) {
		return (getSelectListIndexesForTableAlias(tableAlias).size()!=0 ||
				getClauseListIndexesForTableAlias(tableAlias).size()!=0);
	}
	public void removeFromTableList(String tableAlias, boolean cascade) {
		if (cascade){
			List<Integer> selectIndexes = getSelectListIndexesForTableAlias(tableAlias);
			Collections.sort(selectIndexes);
			Collections.reverse(selectIndexes);
			for(int i : selectIndexes)
				removeSelectColumn(selectList.get(i).alias);

			List<Integer> whereClauseIndexes = getClauseListIndexesForTableAlias(tableAlias);
			Collections.sort(whereClauseIndexes);
			Collections.reverse(whereClauseIndexes);

			for(int i : whereClauseIndexes)
				whereClauses.remove(i);
		}
		tableList.remove(getTableColumnIndex(tableAlias));
	}
	public void setOuterjoinStatus(String tableAlias, boolean selected) {
		tableList.get(getTableColumnIndex(tableAlias)).outerjoined = selected;
	}	


	
	public void addWhereClause(String clause) {
		if(clause !=null && clause.length() != 0  && !whereClauses.contains(clause))
			whereClauses.add(clause);
	}
	public void addFilterClause(String clause) {
		if(clause !=null && clause.length() != 0  && !filterClauses.contains(clause))
			filterClauses.add(clause);
	};
	public void addNormalJoinPredicate(boolean datesOnly) {
		TableEntry [] tabs = tableList.toArray(new TableEntry[0]);
		if (!datesOnly)
			for (int i=0; i< tabs.length; i++)
				for (int j=i+1; j< tabs.length; j++)
					for (DBObjectsModelColumn columnI : tabs[i].datamodel.columns)
						for (DBObjectsModelColumn columnJ : tabs[j].datamodel.columns)
							if (columnI.name.equals(columnJ.name) && !columnI.name.toUpperCase().equals(DataModelStatics.VDATE) && !columnI.name.toUpperCase().equals(DataModelStatics.STARTDATE) && !columnI.name.toUpperCase().equals(DataModelStatics.ENDDATE)){
								addWhereClause(tabs[i].alias +"."+columnI.name+" = " +tabs[j].alias +"."+columnJ.name);
								removeSelectExpression(tabs[j].alias +"."+columnJ.name, tabs[i].alias +"."+columnI.name);
							}
		
						
		for (int i=0; i< tabs.length; i++)
			for (int j=i+1; j< tabs.length; j++)
				for (DBObjectsModelColumn columnI : tabs[i].datamodel.columns)
					for (DBObjectsModelColumn columnJ : tabs[j].datamodel.columns)
						if (columnI.name.toUpperCase().equals(DataModelStatics.VDATE)){
							if (columnJ.name.toUpperCase().equals(DataModelStatics.VDATE)){
								addWhereClause(tabs[i].alias +"."+columnI.name+" = " +tabs[j].alias +"."+columnJ.name);
								removeSelectExpression(tabs[j].alias +"."+columnJ.name, tabs[i].alias +"."+columnI.name);
							} else if (columnJ.name.toUpperCase().equals(DataModelStatics.STARTDATE)){
								addWhereClause(tabs[i].alias +"."+columnI.name+" >= " +tabs[j].alias +"."+columnJ.name);
								removeSelectExpression(tabs[j].alias +"."+columnJ.name, null);
								removeSelectExpression(columnJ.name, null);
							} else if (columnJ.name.toUpperCase().equals(DataModelStatics.ENDDATE)){
								addWhereClause(tabs[i].alias +"."+columnI.name+" <= " +tabs[j].alias +"."+columnJ.name);
								removeSelectExpression(tabs[j].alias +"."+columnJ.name, null);
								removeSelectExpression(columnJ.name, null);
							}
						} else if (columnI.name.toUpperCase().equals(DataModelStatics.STARTDATE)){
							if (columnJ.name.toUpperCase().equals(DataModelStatics.VDATE)) {
								addWhereClause(tabs[i].alias +"."+columnI.name+" <= " +tabs[j].alias +"."+columnJ.name);
								removeSelectExpression(tabs[i].alias +"."+columnI.name, null);
								removeSelectExpression(columnI.name, null);
							} else if (columnJ.name.toUpperCase().equals(DataModelStatics.ENDDATE)) {
								addWhereClause(tabs[i].alias +"."+columnI.name+" <= " +tabs[j].alias +"."+columnJ.name);
							}
						} else if (columnI.name.toUpperCase().equals(DataModelStatics.ENDDATE)){
							if (columnJ.name.toUpperCase().equals(DataModelStatics.VDATE)) {
								addWhereClause(tabs[i].alias +"."+columnI.name+" >= " +tabs[j].alias +"."+columnJ.name);
								removeSelectExpression(tabs[i].alias +"."+columnI.name, null);
								removeSelectExpression(columnI.name, null);
							} else if (columnJ.name.toUpperCase().equals(DataModelStatics.STARTDATE)) { 
								addWhereClause(tabs[i].alias +"."+columnI.name+" >= " +tabs[j].alias +"."+columnJ.name);
							}
						}
		// TODO create startdate and enddate in select list
	}	

	
	private void removeSelectExpression(String expression, String replaceExpression) {
		String altAlias = null;
		if(replaceExpression!= null)
			for (SelectEntry se : selectList)
				if (se.expression.equals(replaceExpression))
					altAlias = se.alias;
		
		for (SelectEntry se : new ArrayList<SelectEntry>(selectList))
			if (se.expression.equals(expression))
				removeSelectColumn(se.alias, altAlias);
		
	}


	public class TableEntry extends ExpressionAliasPair{
		public boolean outerjoined;
		DBObjectsModelRowSource datamodel;
		TableEntry (String expression, String alias, boolean outerjoined, DBObjectsModelRowSource datamodel){
			super(expression, alias);
			this.outerjoined = outerjoined;
			this.datamodel   = datamodel;
		}
		public TableEntry(ExpressionAliasPair pair, boolean outerjoined, DBObjectsModelRowSource datamodel) {
			this(pair.expression, pair.alias, outerjoined, datamodel);
		}
		public TableEntry(String expression, String alias, boolean outerjoinded) {
			this(expression,alias, outerjoinded, DBObjectCache.cache.getCache(expression));
		}
		public String toString(){
			return super.toString() + (outerjoined ? "(+)":"");
		}
		public String toSQL(boolean includeOuterJoinMarkup){
			return super.toSQL() + (outerjoined && includeOuterJoinMarkup ? "(+)":"");
		}		
	}
	
	public class SelectEntry extends ExpressionAliasPair{
		public boolean groupBy;
		SelectEntry (String expression, String alias, boolean groupBy){
			super(expression, alias);
			this.groupBy    = groupBy;
		}
		public SelectEntry(ExpressionAliasPair pair, boolean groupBy) {
			this(pair.expression, pair.alias, groupBy);
		}
		public String toString(){
			return super.toString() + (groupBy ? "*":"");
		}
	}
}
