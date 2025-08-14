package org.libreoffice.example.comp;

import com.sun.star.beans.PropertyValue;
import com.sun.star.document.XDocumentInsertable;
import com.sun.star.frame.XController;
import com.sun.star.frame.XDesktop;
import com.sun.star.io.IOException;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.text.*;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.lib.uno.helper.Factory;

import org.libreoffice.example.helper.DialogHelper;

import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.lib.uno.helper.WeakBase;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;


public final class StarterProjectImpl extends WeakBase
   implements com.sun.star.lang.XServiceInfo,
              com.sun.star.task.XJobExecutor
{
    private final XComponentContext m_xContext;
    private final XController controller;
    private static final String m_implementationName = StarterProjectImpl.class.getName();
    private static final String[] m_serviceNames = {
        "org.libreoffice.example.StarterProject" };


    public StarterProjectImpl( XComponentContext context ) throws Exception
    {
        m_xContext = context;
        var factory = (XMultiServiceFactory) UnoRuntime.queryInterface(XMultiServiceFactory.class, m_xContext.getServiceManager());
        var desktop = (XDesktop) UnoRuntime.queryInterface(XDesktop.class, 
    				factory.createInstance("com.sun.star.frame.Desktop"));
        var frame = desktop.getCurrentFrame();
        controller = frame.getController();
    };

    public static XSingleComponentFactory __getComponentFactory( String sImplementationName ) {
        XSingleComponentFactory xFactory = null;

        if ( sImplementationName.equals( m_implementationName ) )
            xFactory = Factory.createComponentFactory(StarterProjectImpl.class, m_serviceNames);
        return xFactory;
    }

    public static boolean __writeRegistryServiceInfo( XRegistryKey xRegistryKey ) {
        return Factory.writeRegistryServiceInfo(m_implementationName,
                                                m_serviceNames,
                                                xRegistryKey);
    }

    // com.sun.star.lang.XServiceInfo:
    public String getImplementationName() {
         return m_implementationName;
    }

    public boolean supportsService( String sService ) {
        int len = m_serviceNames.length;

        for( int i=0; i < len; i++) {
            if (sService.equals(m_serviceNames[i]))
                return true;
        }
        return false;
    }

    public String[] getSupportedServiceNames() {
        return m_serviceNames;
    }
    
    public XTextViewCursor getSelection() {
        XTextViewCursorSupplier supplier = (XTextViewCursorSupplier) UnoRuntime.queryInterface(XTextViewCursorSupplier.class, controller);
        return supplier.getViewCursor();
    }

    public void insertText(String textString) {
        XTextCursor viewCursor = getSelection();

        XText text = viewCursor.getText();
        XTextCursor cursor = text.createTextCursorByRange(viewCursor);
        XTextRange preNewline, postNewline;

        preNewline = text.createTextCursorByRange(viewCursor).getStart();
        postNewline = text.createTextCursorByRange(viewCursor).getEnd();

        // move citation to its own paragraph so its formatting isn't altered automatically
        // because of the text on either side of it
        text.insertControlCharacter(preNewline, ControlCharacter.PARAGRAPH_BREAK, true);
        text.insertControlCharacter(postNewline, ControlCharacter.PARAGRAPH_BREAK, true);

        insertHTML(textString, cursor);

        // remove previously added paragraphs
        preNewline.setString("");
        postNewline.setString("");
    }

    public void insertHTML(String text, XTextCursor cursor) {
        PropertyValue filterName = new PropertyValue();
        filterName.Name = "FilterName";
        filterName.Value = "HTML Document";
        PropertyValue inputStream = new PropertyValue();
        inputStream.Name = "InputStream";
        inputStream.Value = new StringInputStream(text.getBytes(StandardCharsets.ISO_8859_1));

        try {
            ((XDocumentInsertable) UnoRuntime.queryInterface(XDocumentInsertable.class, cursor)).
                    insertDocumentFromURL("private:stream", new PropertyValue[] {filterName, inputStream});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // com.sun.star.task.XJobExecutor:
    public void trigger(String action)
    {
    	switch (action) {
    	case "actionOne":
            insertText("<html><body>Comment: 38 pages, 13 figures, 6 tables. Submitted to Phys. Rev. D</body></html>");
    		break;
    	default:
    		DialogHelper.showErrorMessage(m_xContext, null, "Unknown action: " + action);
    	}
        
    }

}
