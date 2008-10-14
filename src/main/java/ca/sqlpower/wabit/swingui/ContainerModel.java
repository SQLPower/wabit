package ca.sqlpower.wabit.swingui;

import java.util.ArrayList;
import java.util.List;

public class ContainerModel<C extends Object> {

	private List<List<C>> containers;
	
	private String name;
	
	public ContainerModel() {
		containers = new ArrayList<List<C>>();
		name = "";
	}
	
	public void addContainer() {
		containers.add(new ArrayList<C>());
	}
	
	public void addItem(int containerIndex, C item) {
		containers.get(containerIndex).add(item);		
	}
	
	public C getContents(int containerIndex, int containerLocation) {
		return containers.get(containerIndex).get(containerLocation);
	}
	
	public int getContainerCount() {
		return containers.size();
	}
	
	public int getContainerSize(int containerIndex) {
		return containers.get(containerIndex).size();
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
