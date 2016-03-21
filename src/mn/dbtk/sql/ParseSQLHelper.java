package mn.dbtk.sql;

import java.util.ArrayList;
import java.util.List;


public class ParseSQLHelper {

	public static String transformToComparableSQL(String sql) {
		String result = sql.replace("\r"," ").replace("\n"," ").replace("\t"," ").
						replace("  "," ").replace("  "," ").replace("  "," ").replace("  "," ").replace("  "," ").
						replace(" )",")").replace(") ",")").replace(" (","(").replace("( ","(");
		if (result.indexOf(" ") == 0)
			result = result.substring(1);
		if (result.lastIndexOf(" ") == result.length()-1)
			result = result.substring(0, result.length()-1);
		return result;
	}
	
	
	private static void offerAsBlock(List<String> result, String offering){
		offerAsBlock(result, offering, false, false, false, false);
	}
	private static void offerAsBlock(List<String> result, String offering, boolean filterComments, boolean filterSingleQoutes, boolean peelParenthesis, boolean filterPeeledParenthesis){
		if (offering.length() != 0 &&
				(!filterComments || 
						(!offering.startsWith("/*") && !offering.startsWith("--"))) &&
				(!filterSingleQoutes || 
						!offering.startsWith("'")))
			if (!peelParenthesis || !offering.startsWith("(")){
				result.add(offering);
			} else {
				if(!filterPeeledParenthesis)
					result.add("(");
				String strippedOffering = offering.substring(1, offering.length()-1);
				result.addAll(sqlParseBlocks(strippedOffering, filterComments, filterSingleQoutes, peelParenthesis));
				if(!filterPeeledParenthesis)
					result.add(")");
			}	
	}
	static List<String> sqlParseBlocks(String sql){
		return sqlParseBlocks(sql, false, false, false);
	}
	static List<String> sqlParseBlocks(String sql, boolean filterComments){
		return sqlParseBlocks(sql, filterComments, false, false);
	}
	static List<String> sqlParseBlocks(String sql, boolean filterComments, boolean filterSingleQoutes, boolean peelParenthesis){
		return sqlParseBlocks(sql, filterComments, filterSingleQoutes, peelParenthesis, peelParenthesis);
	}
	static List<String> sqlParseBlocks(String sql, boolean filterComments, boolean filterSingleQoutes, boolean peelParenthesis, boolean filterPeeledParenthesis){
		boolean inSingleQoute    = false;
		boolean inDoubleQoute    = false;
		boolean inLineComments   = false;
		boolean inBlockComments  = false;
		int     parenthesisDepth = 0;
		List<String>  result     = new ArrayList<String>();
		StringBuilder stringToken= new StringBuilder();
		
		char prevToken     = 'x';
		
		for (char token : sql.toCharArray()){
			boolean writeBlockBefore  = false;
			boolean transferPrevToken = false;
			boolean writeBlockAfter   = false;
			switch(token){
			case '\'':
				if (!inLineComments && !inBlockComments && !inDoubleQoute){
					inSingleQoute = !inSingleQoute;
					if (parenthesisDepth==0){
						if (inSingleQoute)
							writeBlockBefore = true;
						else 
							writeBlockAfter  = true;
					}
				}
				
				break;
			case '\"':
				if (!inLineComments && !inBlockComments && !inSingleQoute){
					inDoubleQoute = !inDoubleQoute;
					if (parenthesisDepth==0){
						if (inDoubleQoute)
							writeBlockBefore = true;
						else 
							writeBlockAfter  = true;
					}
				}
				break;
			case '*':
				if (prevToken == '/')
					if (!inSingleQoute && !inDoubleQoute && !inLineComments){
						inBlockComments = true;
						if (parenthesisDepth==0){
							writeBlockBefore = true;
							transferPrevToken = true;
						}
					}
				break;
			case '/':
				if (prevToken == '*')
					if (!inSingleQoute && !inDoubleQoute && !inLineComments){
						inBlockComments = false;
						if (parenthesisDepth==0)
							writeBlockAfter = true;
					}
				break;
			case '-':
				if (prevToken == '-')
					if (!inSingleQoute && !inDoubleQoute && !inBlockComments){
						inLineComments = true;
						if (parenthesisDepth==0) {
							writeBlockBefore = true;
							transferPrevToken = true;
						}
					}
				break;
			case '\n':
			case '\r':
				inLineComments = false;
				if (parenthesisDepth==0)
					writeBlockAfter = true;
				break;
			case '(':
				if (!inSingleQoute && !inDoubleQoute && !inBlockComments && !inLineComments){
					if (parenthesisDepth ==0){
						writeBlockBefore = true;
					}
					parenthesisDepth++;
				}
				break;				
			case ')':
				if (!inSingleQoute && !inDoubleQoute && !inBlockComments && !inLineComments){
					parenthesisDepth--;
					if (parenthesisDepth ==0){
						writeBlockAfter = true;
					}
				}
				break;	
			case ' ':
			case '\t':
			case ',':
				if (!inSingleQoute && !inDoubleQoute && !inBlockComments && !inLineComments && parenthesisDepth==0){
					writeBlockBefore = true;
					writeBlockAfter = true;
				}
				break;
			}
			
			if (writeBlockBefore && stringToken.length() != 0){
				if (transferPrevToken)
					stringToken.deleteCharAt(stringToken.length()-1);
				
				offerAsBlock(result, stringToken.toString(), filterComments, filterSingleQoutes, peelParenthesis, filterPeeledParenthesis);

				stringToken = new StringBuilder();
				if (transferPrevToken)
					stringToken.append(prevToken);
			}
			stringToken.append(token);
			if (writeBlockAfter && stringToken.length() != 0){

				offerAsBlock(result, stringToken.toString(), filterComments, filterSingleQoutes, peelParenthesis, filterPeeledParenthesis);

				stringToken = new StringBuilder();				
			}	
			
			prevToken = token;
		}
		
		offerAsBlock(result, stringToken.toString(), filterComments, filterSingleQoutes, peelParenthesis, filterPeeledParenthesis);
		
		return result;
	}
	
	static List<String> blocksToListEntrees(List<String> blocks, String separator){
		return blocksToListEntrees(blocks, java.util.Arrays.asList(new String[]{separator}));
	}

	static List<String> blocksToListEntrees(List<String> blocks, List<String> separators){
		List<String>  result      = new ArrayList<String>();
		List<String>  separatorsUC       = new ArrayList<String>();
		for (String separator: separators)
			separatorsUC.add(separator.toUpperCase());
		
		StringBuilder stringToken = new StringBuilder();
		for (String s : blocks){
			if (s.startsWith("(") || s.startsWith("/*") || s.startsWith("--") || s.startsWith("\"") || s.startsWith("'")){
				stringToken.append(s);
			} else {
				String remainder = s;
				String matchSep = "";
				while (remainder.length() > 0){
					int j=-1;
					for (String sep : separatorsUC){
						int i = remainder.indexOf(sep);
						if (i>= 0 && (j<0 || i<j)){
							matchSep = sep;
							j=i;
						}
					}
					
					if (j != -1){
						if (j>0)
							stringToken.append(remainder.substring(0, j-1));
						offerAsBlock(result, stringToken.toString());
						stringToken = new StringBuilder();
						remainder   = remainder.length() ==j ? "" : remainder.substring(j+matchSep.length());
					} else {
						stringToken.append(s);
						remainder = "";
					}
				}
			}
		}
		offerAsBlock(result, stringToken.toString().trim());
		
		return result;
	}
	
	static boolean expressionContainsVariable(String expression, String variable, boolean acceptDotBefore, boolean acceptDotAfter){
		return expressionReplaceVariable(expression, variable, "", acceptDotBefore, acceptDotAfter).length() != expression.length();
	}

	static String expressionReplaceVariable(String expression, String find, String replace, boolean acceptDotBefore, boolean acceptDotAfter){
		List<String> sd = sqlParseBlocks(expression, false, false, true, false);
		StringBuffer sb = new StringBuffer();
		for (String s : sd){
			if (s.startsWith("'") || s.startsWith("--") || s.startsWith("/*")){
				sb.append(s);
			} else if (s.startsWith("\"") && s.equals(find)){
				sb.append(replace);
			} else {
				String beforeRegEx = acceptDotBefore ? "(\\A|[^\\w])" : "(\\A|[^\\w\\.])";
				String afterRegEx  = acceptDotAfter ? "(\\z|[^\\w])" : "(\\z|[^\\w\\.])";
				sb.append(s.replaceAll(beforeRegEx + "(?i:"+ find + ")" + afterRegEx, "$1"+replace+"$2"));
			}
		}
		return sb.toString();
	}	
	
	
	static int lastIndexOfSQLBlockAware(String string, String search) {
		int result = -1;
		int passedChars = 0;
		for(String token : sqlParseBlocks(string)){
			if (!token.startsWith("\"") && !token.startsWith("'") && !token.startsWith("--") && !token.startsWith("/*")){
				int lio = token.lastIndexOf(search);
				if (lio != -1)
					result = lio + passedChars;
			}
			passedChars += token.length();
		}
		return result;
	}

	// Alias related stuff
	static ExpressionAliasPair findAlias(String entryIn) {
		String entry = entryIn.trim();
		int lastSpace = lastIndexOfSQLBlockAware(entry, " ");
		if (lastSpace == -1){
			int lastDot = lastIndexOfSQLBlockAware(entry, ".");
			if (lastDot == -1){
				return new ExpressionAliasPair (entry, safeAlias(entry));
			} else {
				return new ExpressionAliasPair (entry, safeAlias(entry.substring(lastDot+1).trim()));
			} 
		} else {
			String exprBeforeLastWhiteSpace = entry.substring(0,lastSpace).trim();
			String exprAfterLastWhiteSpace  = entry.substring(lastSpace).trim();
			char lastCharBeforeLastWhitespace = exprBeforeLastWhiteSpace.charAt(exprBeforeLastWhiteSpace.length()-1);
			switch (lastCharBeforeLastWhitespace){
			case '+':
			case '-':
			case '*':
			case '/':
			case '|':
			case '=':									
				return new ExpressionAliasPair (entry, safeAlias(entry));
			default:
				return new ExpressionAliasPair (exprBeforeLastWhiteSpace, safeAlias(exprAfterLastWhiteSpace));
			}
		}
	}

	private static String safeAlias(String source) {
		String cleaned = source.replace(" ","").replace("\t","").replace("\r","").replace("\n","");
		if (source.startsWith("\"") && source.endsWith("\""))
			cleaned = source.substring(1,source.length()-1);
		
		if (cleaned.length()>30)
			cleaned = cleaned.substring(0,29);
		
		if (cleaned.equals(source))
			return source.toUpperCase();
		
		return "\"" + cleaned + "\"";
	}

	static String cleanFormatRawAlias(String aliasRaw) {
		return aliasRaw.startsWith("\"") ? aliasRaw : aliasRaw.toUpperCase();
	}
	public static String stripQoutes(String alias) {
		return alias.startsWith("\"") ? alias.substring(1,alias.length()-1).replaceAll("\"\"","\""): alias.toUpperCase();
	}
	public static String wrapQoutesWhenRequired(String alias){
		if (!alias.matches("[a-zA-Z]\\w*+"))
			return "\"" + alias.replace("\"","\"\"") + "\"";
		
		return alias;
	}
	
	

	static class ExpressionAliasPair{
		public String expression;
		public String alias;		
		ExpressionAliasPair (String expression, String aliasRaw){
			this.expression  = expression;
			this.alias       = cleanFormatRawAlias(aliasRaw);
		}
		public String toString(){
			return expression + " " + alias;
		}
		public String toSQL(){
			if (expression.endsWith("."+alias) || expression.equals(alias)){
				return expression;
			} else {
				return expression + " " + alias;
			}
		}
	}

}
