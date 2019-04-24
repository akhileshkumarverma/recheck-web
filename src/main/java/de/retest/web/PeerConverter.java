package de.retest.web;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import de.retest.recheck.ui.DefaultValueFinder;
import de.retest.recheck.ui.descriptors.RootElement;
import de.retest.recheck.ui.descriptors.idproviders.RetestIdProvider;

class PeerConverter {

	private final RetestIdProvider idProvider;

	private final AttributesProvider attributesProvider;

	private final Map<String, Map<String, Object>> data;
	private final BufferedImage screenshot;
	private final String title;

	private final Map<String, WebElementPeer> converted = new HashMap<>();
	private RootElementPeer root = null;

	private final DefaultValueFinder defaultValueFinder;

	public PeerConverter( final RetestIdProvider idProvider, final AttributesProvider attributesProvider,
			final Map<String, Map<String, Object>> data,
			final String title, final BufferedImage screenshot, final DefaultValueFinder defaultValueFinder ) {
		this.idProvider = idProvider;
		this.attributesProvider = attributesProvider;
		this.data = data;
		this.title = title;
		this.screenshot = screenshot;
		this.defaultValueFinder = defaultValueFinder;
	}

	public RootElement convertToPeers() {
		idProvider.reset();
		for ( final Map.Entry<String, Map<String, Object>> entry : data.entrySet() ) {
			final String path = entry.getKey();
			final WebData webData = new WebData( entry.getValue() );
			convertToPeer( path, webData );
		}

		if ( root == null ) {
			throw new NullPointerException( "RootElementPeer is null." );
		}

		return root.toElement( null );
	}

	private WebElementPeer convertToPeer( final String path, final WebData webData ) {
		final String parentPath = getParentPath( path );
		WebElementPeer peer = converted.get( path );

		if ( peer != null ) {
			return peer;
		}

		if ( parentPath == null ) {
			assert root == null : "We can only have one root element!";
			root = new RootElementPeer( attributesProvider, webData, path, title, screenshot, defaultValueFinder );
			peer = root;
		} else {
			peer = new WebElementPeer( attributesProvider, webData, path, defaultValueFinder );
			WebElementPeer parent = converted.get( parentPath );
			if ( parent == null ) {
				parent = convertToPeer( parentPath, new WebData( data.get( parentPath ) ) );
			}
			parent.addChild( peer );
		}

		converted.put( path, peer );
		return peer;
	}

	static String getParentPath( final String path ) {
		final String parentPath = path.substring( 0, path.lastIndexOf( '/' ) );
		if ( parentPath.length() == 1 ) {
			return null;
		}
		return parentPath;
	}
}
