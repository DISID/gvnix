package org.springframework.roo.addon.json;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

/**
 * Commands for addon-json
 * 
 * @author Stefan Schmidt
 * @since 1.1
 */
@Component 
@Service 
public class JsonCommands implements CommandMarker {
	@Reference private JsonOperations operations;

	@CliAvailabilityIndicator({ "json setup", "json add", "json all" }) 
	public boolean isPropertyAvailable() {
		return operations.isCommandAvailable();
	}

	@CliCommand(value = "json add", help = "Adds @RooJson annotation to target type") 
	public void add(
		@CliOption(key = "class", mandatory = false, unspecifiedDefaultValue = "*", optionContext = "update,project", help = "The java type to apply this annotation to") JavaType target,
		@CliOption(key = "rootName", mandatory = false, help = "The root name which should be used to wrap the JSON document") String rootName) {
		
		operations.annotateType(target, rootName);
	}
	
	@CliCommand(value = "json all", help = "Adds @RooJson annotation to all types annotated with @RooJavaBean") 
	public void all() {
		operations.annotateAll();
	}
}