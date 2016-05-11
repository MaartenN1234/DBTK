package mn.dbtk.sql.dbcache;

import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.JComboBox;

public class DBObjectCacheSyncedGUIElements {
	public static class RowsourcesComboBox extends JComboBox<String> implements DBObjectCacheLoadListener{
		private boolean cacheLoading = false;
		private static ItemListener[] NO_LISTENERS =  new ItemListener[0];
		public RowsourcesComboBox(String setSelectedItem){
			super();
			cacheLoaded(setSelectedItem);
			DBObjectCache.cache.addCacheLoadListener(this);
		}
		
		public ItemListener[] getItemListeners() {
			return cacheLoading ? NO_LISTENERS : super.getItemListeners();
			
		}
		
		public void cacheLoaded(){
			cacheLoaded((String) getSelectedItem());
		}
		public void cacheLoaded(String selection){
			Set<String> allRowsources = DBObjectCache.cache.getAllRowSources();
			List<String> allRowsourcesList = new ArrayList<String>(allRowsources);
			if (selection != null && !allRowsources.contains(selection)){
				allRowsourcesList.add(selection);
			}
			Collections.sort(allRowsourcesList);
			
			cacheLoading = true;
			
			removeAllItems();
					
			for (String rowSource : allRowsourcesList)
				addItem(rowSource);
			
			cacheLoading = false;
			
			setSelectedItem(selection);
			validate();			
		}
	}
}
