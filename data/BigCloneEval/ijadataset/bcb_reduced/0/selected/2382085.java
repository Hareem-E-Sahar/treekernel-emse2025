package com.prime.yui4jsf.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.prime.yui4jsf.filter.YUI4JSFFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This NestedResourcesHandler class is responsible for
 * Handling the nested resources like images that
 * resides at the same path or the relative path of 
 * another resource file like css files.
 *
 * This case happens many times at YUI library.
 *
 * $Author: hazem_saleh $Date: Sat, 23 Dec 2007
 */
public class NestedResourcesHandler {

    private static final Log logger = LogFactory.getLog(NestedResourcesHandler.class);

    private static final String specialCharacters = "\\.\\/\\_\\!\\#\\$\\%\\&\\-\\+";

    private static final String filePathChar = "[a-zA-Z0-9" + specialCharacters + "]";

    private static final String imagesFilesRegex = "(" + filePathChar + "+(\\.)png)|" + "'" + filePathChar + "+(\\.)png'|" + "\"" + filePathChar + "+(\\.)png\"|" + "(" + filePathChar + "+(\\.)jpg)|" + "'" + filePathChar + "+(\\.)jpg'|" + "\"" + filePathChar + "+(\\.)jpg\"|" + "(" + filePathChar + "+(\\.)gif)|" + "'" + filePathChar + "+(\\.)gif'|" + "\"" + filePathChar + "+(\\.)gif\"";

    private static final String MOVE_UP_ONE_LEVEL = "..";

    private static final String SLASH = "/";

    /**
	 * getCurrentFileDirectoryPath is used for getting the directory path
	 * of the resource/css file
	 * Like) skins/sam/picker.css will return skins/sam/
	 * @param currentFileNamePath
	 * @return
	 */
    private static String getCurrentFileDirectoryPath(String currentFileNamePath) {
        return currentFileNamePath.substring(0, currentFileNamePath.lastIndexOf("/") + 1);
    }

    /**
	 * The handleNestedImages method is used for generating suitable PL
	 * urls to the nested images inside the resource/css file byte [].
	 * @param resourceFolder
	 * @param resourceName
	 * @param isCustomResource
	 * @param isResourceFromAssetsFolder
	 * @param srcBytesBuff
	 * @return the new bytes after the path issue resolved
	 */
    public static byte[] handleNestedImages(String resourceFolder, String resourceName, boolean isCustomResource, boolean isResourceFromAssetsFolder, byte[] srcBytesBuff) {
        String fullResourcePath = Yui4JSFResourceLoaderPhaseListener.populateResourcePath(resourceFolder, resourceName, isCustomResource, isResourceFromAssetsFolder);
        int start = 0, end = 0;
        String srcBuffer = new String(srcBytesBuff);
        Pattern pattern = Pattern.compile(imagesFilesRegex);
        Matcher matcher = pattern.matcher(srcBuffer);
        String output = "";
        while (matcher.find()) {
            List fullDirectoryPathTokens = getFullResourcePathTokens(fullResourcePath);
            if (srcBuffer.charAt(matcher.start()) == '\"' || srcBuffer.charAt(matcher.start()) == '(' || srcBuffer.charAt(matcher.start()) == '\'') {
                start = matcher.start() + 1;
                end = matcher.end() - 1;
            } else {
                start = matcher.start();
                end = matcher.end();
            }
            String beforeContent = srcBuffer.substring(0, start);
            output += beforeContent;
            String imagePathString = srcBuffer.substring(start, end);
            List imagePathStringTokens = getImagePathStringTokens(imagePathString);
            boolean isUnderCurrentDirectory = isUnderCurrentDirectory(imagePathStringTokens);
            if (isUnderCurrentDirectory) {
                output += "yui4jsfResources.jsf?name=" + getCurrentFileDirectoryPath(resourceName) + srcBuffer.substring(start, end) + "&folder=" + resourceFolder + "&fromAssets=" + isResourceFromAssetsFolder + "&custom=" + isCustomResource;
            } else {
                output += applyFolderNavigation(fullDirectoryPathTokens, imagePathStringTokens);
            }
            srcBuffer = srcBuffer.substring(end);
            matcher = pattern.matcher(srcBuffer);
        }
        output += srcBuffer;
        return output.getBytes();
    }

    /**
	 * applyFolderNavigation is used for parsing the imagePath string and then
	 * constructing the suitable PL reference
	 * @param fullDirectoryPathTokens is the List that contains the tokens of the
	 * resource/css file path 
	 * for example) colorpicker/assets/skins/sam/ under which picker.css exist
	 * @param imagePathStringTokens is the String of the image path tokens
	 * for example) ../../../../assets/skins/sam/sprite.png
	 * @return The final suitable YUI4JSF PL url
	 */
    private static String applyFolderNavigation(List fullDirectoryPathTokens, List imagePathStringTokens) {
        while (!imagePathStringTokens.isEmpty()) {
            if (MOVE_UP_ONE_LEVEL.equals(imagePathStringTokens.get(0))) {
                if (fullDirectoryPathTokens.size() == 0) {
                    logger.warn("Panic! image path is not correct " + "inside the resource under" + ListToString(fullDirectoryPathTokens, false));
                    return "";
                }
                fullDirectoryPathTokens.remove(fullDirectoryPathTokens.size() - 1);
                imagePathStringTokens.remove(0);
            } else {
                break;
            }
        }
        String correctImagePath = ListToString(fullDirectoryPathTokens, true) + ListToString(imagePathStringTokens, false);
        String resourceName = YUI4JSFFilter.getResourceFileName(correctImagePath);
        String resourceFolder = YUI4JSFFilter.getResourceFolder(correctImagePath);
        boolean isCustomResource = YUI4JSFFilter.isCustom(correctImagePath);
        boolean isResourceFromAssetsFolder = YUI4JSFFilter.isFromAssets(correctImagePath);
        String output = "yui4jsfResources.jsf?name=" + resourceName + "&folder=" + prepareFolderPathForPL(resourceFolder) + "&fromAssets=" + isResourceFromAssetsFolder + "&custom=" + isCustomResource;
        return output;
    }

    /**
	 * This method prepares the folder path for the YUI4JSF PL
	 * @param resourceFolder the path of the folder
	 */
    private static String prepareFolderPathForPL(String resourceFolder) {
        int index = resourceFolder.indexOf(Yui4JSFConstants.YUI4JSF_COMPONENT_DEFAULT_RESOURCE_FOLDER);
        if (index == -1) return resourceFolder;
        return resourceFolder.replace(Yui4JSFConstants.YUI4JSF_COMPONENT_DEFAULT_RESOURCE_FOLDER, "");
    }

    /**
	 * The isUnderCurrentDirectory check whether the image resides in the same path
	 * of the resource/css file
	 * @param imagePathStringTokens is the list of the path tokens
	 * @return boolean
	 */
    private static boolean isUnderCurrentDirectory(List imagePathStringTokens) {
        return ((String) imagePathStringTokens.get(0)).equals(MOVE_UP_ONE_LEVEL) ? false : true;
    }

    /**
	 * The getFullResourcePathTokens is used for tokenizing the fullResourcePath
	 * and set the tokens in the result list
	 * @param fullResourcePath is the String of the resource/css path
	 * @return the List of tokens
	 */
    private static List getFullResourcePathTokens(String fullResourcePath) {
        List fullResourcePathPartsList = new ArrayList();
        String[] fullResourcePathParts = fullResourcePath.split(SLASH);
        for (int i = 0; i < fullResourcePathParts.length - 1; ++i) {
            fullResourcePathPartsList.add(fullResourcePathParts[i]);
        }
        return fullResourcePathPartsList;
    }

    /**
	 * The getImagePathStringTokens is used for tokenizing the imagePath 
	 * String that resides inside the resource/css file
	 * and set the tokens in the result list
	 * @param imagePathString is the String of the image path inside the 
	 * resource/css file.
	 * @return the List of tokens
	 */
    private static List getImagePathStringTokens(String imagePathString) {
        List imagePathStringPartsList = new ArrayList();
        String[] imagePathStringParts = imagePathString.split(SLASH);
        for (int i = 0; i < imagePathStringParts.length; ++i) {
            imagePathStringPartsList.add(imagePathStringParts[i]);
        }
        return imagePathStringPartsList;
    }

    /**
	 * Utility function that converts the List of tokens to a readable 
	 * String path
	 * @param tokens
	 * @param finalSlash determines whether to put a / at the end of the readable 
	 * String or not
	 * @return the readable String path
	 */
    public static String ListToString(List tokens, boolean finalSlash) {
        String result = "";
        for (int i = 0; i < tokens.size(); ++i) {
            result += tokens.get(i);
            if (i != tokens.size() - 1) {
                result += "/";
            } else if (finalSlash) {
                result += "/";
            }
        }
        return result;
    }
}
