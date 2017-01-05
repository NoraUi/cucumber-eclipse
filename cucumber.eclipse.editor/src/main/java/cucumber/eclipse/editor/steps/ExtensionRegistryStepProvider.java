package cucumber.eclipse.editor.steps;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import cucumber.eclipse.steps.integration.IStepDefinitions;
import cucumber.eclipse.steps.integration.IStepListener;
import cucumber.eclipse.steps.integration.Step;
import cucumber.eclipse.steps.integration.StepsChangedEvent;

public class ExtensionRegistryStepProvider implements IStepProvider, IStepListener {

	final static String EXTENSION_POINT_STEPDEFINITIONS_ID = "cucumber.eclipse.steps.integration";

	private Set<Step> steps = new HashSet<Step>();

	private List<IStepDefinitions> stepDefinitions = getStepDefinitions();

	private IFile file;
	
	public ExtensionRegistryStepProvider(IFile file) {
		this.file = file;
		reloadSteps();
		addStepListener(this);
	}

	public void addStepListener(IStepListener listener) {
		for (IStepDefinitions stepDef : stepDefinitions) {
			stepDef.addStepListener(listener);
		}
	}

	public Set<Step> getStepsInEncompassingProject() {
		return steps;
	}

	private void reloadSteps() {
		steps.clear();
		for (IStepDefinitions stepDef : stepDefinitions) {
			steps.addAll(stepDef.getSteps(file));
		}
	}

	public void removeStepListener(IStepListener listener) {
		for (IStepDefinitions stepDef : stepDefinitions) {
			stepDef.removeStepListener(listener);
		}
	}

	@Override
	public void onStepsChanged(StepsChangedEvent event) {
		reloadSteps();
	}

	private static List<IStepDefinitions> getStepDefinitions() {
		List<IStepDefinitions> stepDefs = new ArrayList<IStepDefinitions>();
		IConfigurationElement[] config = Platform
				.getExtensionRegistry()
				.getConfigurationElementsFor(EXTENSION_POINT_STEPDEFINITIONS_ID);
		try {
			for (IConfigurationElement ce : config) {
				Object obj = ce.createExecutableExtension("class");
				if (obj instanceof IStepDefinitions) {
					stepDefs.add((IStepDefinitions) obj);
				}
			}
		} catch (CoreException e) {
		}
		return stepDefs;
	}
}
