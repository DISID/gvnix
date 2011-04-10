package org.springframework.roo.classpath.itd;

import org.springframework.roo.classpath.details.ItdTypeDetails;
import org.springframework.roo.classpath.scanner.MemberDetailsScanner;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.metadata.MetadataItem;
import org.springframework.roo.support.util.Assert;

/**
 * Simplifies the development of {@link ItdMetadataProvider}s that wish to automatically discover
 * new {@link ItdTypeDetails} that become available elsewhere in the system, even if the
 * {@link ItdMetadataProvider} is not registered as a downstream dependency.
 * 
 * <p>
 * This class helps solves the common requirement of not knowing which other add-ons might be providing
 * metadata you are interested in. While {@link MemberDetailsScanner} will locate all metadata presently
 * available in the system, this is a snapshot of metadata at that moment in time. While it's simple (and
 * normal practice) to register as a downstream dependency of scanned metadata that you wish to monitor,
 * this approach is insufficient to discover new metadata that subsequently becomes available (or even
 * discover metadata that previously was available but did not contain members of interest at that moment
 * in time and were therefore not registered as a dependency to monitor).
 * 
 * <p>
 * The practical solution to these problems is to subclass this class, implement the abstract
 * {@link #getLocalMidToRequest(ItdTypeDetails)}, and in the activate method register a generic
 * listener as described in the documentation for {@link #notifyForGenericListener(String)}. This class
 * will then receive all generic notifications, determine if they relate to an ITD, extract the
 * {@link ItdTypeDetails}, and present it to {@link #getLocalMidToRequest(ItdTypeDetails)}. The
 * latter method allows the subclass to decide if they want to be formally asked for new metadata,
 * in which case this class will do so. Importantly the subclass can decide the exact metadata identification
 * string to request, allowing flexibility in implementation (eg a subclass could monitor for
 * new ITD metadata related to types other than simply their normal governor types).
 * 
 * @author Ben Alex
 * @since 1.1.1
 *
 */
public abstract class AbstractMemberDiscoveringItdMetadataProvider extends AbstractItdMetadataProvider {
	
	/**
	 * Receives generic notification events. You must still register in the activate method to receive
	 * these events, as described in the JavaDocs for the superclass method of the same name.
	 * 
	 * @see AbstractItdMetadataProvider#notifyForGenericListener(String)
	 */
	protected final void notifyForGenericListener(String upstreamDependency) {
		// Receives event arising from our MetadataDependencyRegistry.addNotificationListener(this) method.
		// We do this to discover new ITDs that offer setter methods in the @RooDataOnDemand.entity=SomeClass.class.
		
		if (MetadataIdentificationUtils.isIdentifyingClass(upstreamDependency)) {
			// It's just a class-specific notification (ie no instance), so we don't care about it
			return;
		}
		
		// To get here we have an instance-specific identifier.
		// Let's try to grab the metadata
		MetadataItem metadataItem = metadataService.get(upstreamDependency);
		
		// We don't have to worry about physical type metadata, as we monitor the relevant .java once the DOD governor is first detected
		if (metadataItem == null || !metadataItem.isValid() || !(metadataItem instanceof ItdTypeDetailsProvidingMetadataItem)) {
			// There's something wrong with it or it's not for an ITD, so let's gracefully abort
			return;
		}
		
		// Let's ensure we have some ITD type details to actually work with
		ItdTypeDetailsProvidingMetadataItem itdMetadata = (ItdTypeDetailsProvidingMetadataItem) metadataItem;
		ItdTypeDetails itdTypeDetails = itdMetadata.getMemberHoldingTypeDetails();
		if (itdTypeDetails == null) {
			return;
		}

		// Ask the subclass if they'd like us to request a MetadataItem in response
		String localMid = getLocalMidToRequest(itdTypeDetails);
		if (localMid != null) {
			Assert.isTrue(MetadataIdentificationUtils.isIdentifyingInstance(localMid), "Metadata identification string '" + localMid + "' should identify a specific instance to request");
			Assert.isTrue(MetadataIdentificationUtils.getMetadataClass(localMid).equals(MetadataIdentificationUtils.getMetadataClass(getProvidesType())), "Metadata identication string '" + MetadataIdentificationUtils.getMetadataClass(localMid) + "' is incompatible with this metadata provider's class '" + MetadataIdentificationUtils.getMetadataClass(getProvidesType()) + "'");
			metadataService.get(localMid, true);
		}
	}
	
	/**
	 * Allows a subclass to assess a recently updated {@link ItdTypeDetails} and decide whether they are interested
	 * in a metadata request being made in response. Subclasses will generally iterate over the passed ITD details
	 * and search for members of interest. If any members of interest are located, subclasses will return an
	 * instance-specific metadata identification string (MID) consistent with the subclass' {@link #getProvidesType()}
	 * The requested MID will be cleared from the cache and formally requested. This process allows subclasses to
	 * effectively discover new ITD members that appear over time without needing to process every request themselves.
	 * 
	 * @param itdTypeDetails a non-null, valid ITD type details from which member information is available (never null)
	 * @return null if the subclass is not interested in the type, or a MID if it is
	 */
	protected abstract String getLocalMidToRequest(ItdTypeDetails itdTypeDetails);

}
