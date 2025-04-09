public class Test {	public String getToolTipText(final int aFlags) {
		Flags flags = Flags.valueOf(aFlags);

		HTMLBuilder builder = new HTMLBuilder();
		builder.beginHTML();
		builder.beginDoc();
		
		builder.beginTag(
			"table",
			"border", 0
		);

		// name
		if (flags.isClear(NO_NAME_INFO))
			addInfo(builder, TK.centerSqueeze(toString(), 128), null);

		// type
		String type = getFileExtension();
		// dynamic folder/folder
		if (type.isEmpty()) {
			if (isDynamicFolder())
				type = _("Virtual Folder");
		}
		// file
		else {
			PluginInfo info = EditorPlugin.findPluginForExtension(type);
			// format: Type Description (ext)
			type = ((info == null) ? _("Unknown") : (info.name.get()) + " (" + type + ")");
		}
		if (!type.isEmpty())
			addInfo(builder, _("Type:"), type);

		if (!TK.isEmpty(comment) && flags.isClear(NO_COMMENT_INFO)) {
			// get first line from a multiline comment
			String[] s = TK.splitPair(comment.trim(), '\n', TK.SPLIT_PAIR_NULL_ERROR);
			addInfo(
				builder,
				_("Comment:"),
				TK.centerSqueeze((s == null) ? comment : (s[0] + "..."), 128)
			);
		}

		if ((tags != null) && flags.isClear(NO_TAGS_INFO))
			addInfo(builder, _("Tags:"), TagsUtils.getLocalizedTags(tags));

		if (categories != null)
			addInfo(builder, _("Categories:"), categories);

		if (flags.isClear(NO_DATE_INFO)) {
			long createdTime = getCreatedTime();
			if (createdTime != 0)
				addDateInfo(builder, _("Created:"), createdTime);
			if (isFile())
				addDateInfo(builder, _("Modified:"), getFile().lastModified());
		}
		
		if (flags.isClear(NO_LOCATION_INFO))
			addInfo(builder, _("Location:"), TK.centerSqueeze(getNicePath(), 128));

		File mount = getMountDirectory();
		if (mount != null)
			addInfo(builder, _("Mounted External Directory:"), mount);

		if (!isWriteable())
			addWarning(builder, _("This item is not writeable (read only)"));
		else if (isLocked() && flags.isClear(NO_LOCK_INFO))
			addWarning(builder, _("This item is locked (read only)"));

		if (flags.isClear(NO_DATE_INFO)) {
			boolean isSeparator = false;
			long openTime = getLastOpenTime();
			if (isFile() && (openTime != 0)) {
				addSeparator(builder);
				isSeparator = true;
				addDateInfo(builder, _("Last Opened:"), openTime);
			}
			long age = getAge();
			if (age != -1) {
				if (!isSeparator)
					addSeparator(builder);
				addInfo(builder, _("Age:"), _("{0} day(s)", age));
			}
		}
		
		// file size
		if (isFile()) {
			addInfo(builder, _("Size:"), MFormat.toAutoSize(getFile().length()));
		}
		
		if (Vars.treeShowMetaInfo.get()) {
			MProperties metaProperties = getProperty(PREVIEW_PROPERTIES, null);
			if (metaProperties != null) {
				String imageType = metaProperties.getProperty(Preview.IMAGE_TYPE_PROPERTY, "");
				if (!TK.isEmpty(imageType)) {
					addSeparator(builder);
					addInfo(builder, _("Dimensions:"), String.format(
						"%s x %s",
						metaProperties.getProperty(Preview.IMAGE_WIDTH_PROPERTY, "?"),
						metaProperties.getProperty(Preview.IMAGE_HEIGHT_PROPERTY, "?")
					));
					addInfo(builder, _("Type:"), imageType);
					addInfo(builder, "bpp:", metaProperties.getProperty(Preview.IMAGE_BPP_PROPERTY, "?"));
				}
				String url = metaProperties.getProperty(Preview.URL_PROPERTY, null);
				if (url != null) {
					addSeparator(builder);
					addInfo(builder, "URL:", TK.centerSqueeze(url, 128));
				}
			}
		}
		
		if (getFS() instanceof FSProperties)
			FSProperties.class.cast(getFS()).updateInfo(this, builder);
		
		builder.endTag("table");
		builder.endDoc();
		
		// TEST: MLogger.debug("fs", builder.toString());
		
		// 75 - no HTML content
		return (builder.length() == 75) ? null : builder.toString();
	}
}