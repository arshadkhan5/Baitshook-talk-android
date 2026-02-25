/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-FileCopyrightText: 2017-2019 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccFilebrowser.webdav;

import com.gcc.talk.gccFilebrowser.models.properties.GccNCEncrypted;
import com.gcc.talk.gccFilebrowser.models.properties.GccNCPermission;
import com.gcc.talk.gccFilebrowser.models.properties.GccNCPreview;
import com.gcc.talk.gccFilebrowser.models.properties.GccOCFavorite;
import com.gcc.talk.gccFilebrowser.models.properties.GccOCId;
import com.gcc.talk.gccFilebrowser.models.properties.GccOCSize;

import java.util.ArrayList;
import java.util.List;

import at.bitfire.dav4jvm.Property;
import at.bitfire.dav4jvm.PropertyRegistry;
import at.bitfire.dav4jvm.property.CreationDate;
import at.bitfire.dav4jvm.property.DisplayName;
import at.bitfire.dav4jvm.property.GetContentLength;
import at.bitfire.dav4jvm.property.GetContentType;
import at.bitfire.dav4jvm.property.GetETag;
import at.bitfire.dav4jvm.property.GetLastModified;
import at.bitfire.dav4jvm.property.ResourceType;

public class GccDavUtils {

    public static final String OC_NAMESPACE = "http://owncloud.org/ns";
    public static final String NC_NAMESPACE = "http://nextcloud.org/ns";
    public static final String DAV_PATH = "/remote.php/dav/files/";

    public static final String EXTENDED_PROPERTY_NAME_PERMISSIONS = "permissions";
    public static final String EXTENDED_PROPERTY_NAME_REMOTE_ID = "id";
    public static final String EXTENDED_PROPERTY_NAME_SIZE = "size";
    public static final String EXTENDED_PROPERTY_FAVORITE = "favorite";
    public static final String EXTENDED_PROPERTY_IS_ENCRYPTED = "is-encrypted";
    public static final String EXTENDED_PROPERTY_MOUNT_TYPE = "mount-type";
    public static final String EXTENDED_PROPERTY_OWNER_ID = "owner-id";
    public static final String EXTENDED_PROPERTY_OWNER_DISPLAY_NAME = "owner-display-name";
    public static final String EXTENDED_PROPERTY_UNREAD_COMMENTS = "comments-unread";
    public static final String EXTENDED_PROPERTY_HAS_PREVIEW = "has-preview";
    public static final String EXTENDED_PROPERTY_NOTE = "note";

    // public static final String TRASHBIN_FILENAME = "trashbin-filename";
    // public static final String TRASHBIN_ORIGINAL_LOCATION = "trashbin-original-location";
    // public static final String TRASHBIN_DELETION_TIME = "trashbin-deletion-time";

    // public static final String PROPERTY_QUOTA_USED_BYTES = "quota-used-bytes";
    // public static final String PROPERTY_QUOTA_AVAILABLE_BYTES = "quota-available-bytes";

    static Property.Name[] getAllPropSet() {
        List<Property.Name> props = new ArrayList<>(20);

        props.add(DisplayName.NAME);
        props.add(GetContentType.NAME);
        props.add(GetContentLength.NAME);
        props.add(GetContentType.NAME);
        props.add(GetContentLength.NAME);
        props.add(GetLastModified.NAME);
        props.add(CreationDate.NAME);
        props.add(GetETag.NAME);
        props.add(ResourceType.NAME);

        props.add(GccNCPermission.NAME);
        props.add(GccOCId.NAME);
        props.add(GccOCSize.NAME);
        props.add(GccOCFavorite.NAME);
        props.add(new Property.Name(OC_NAMESPACE, EXTENDED_PROPERTY_OWNER_ID));
        props.add(new Property.Name(OC_NAMESPACE, EXTENDED_PROPERTY_OWNER_DISPLAY_NAME));
        props.add(new Property.Name(OC_NAMESPACE, EXTENDED_PROPERTY_UNREAD_COMMENTS));

        props.add(GccNCEncrypted.NAME);
        props.add(new Property.Name(NC_NAMESPACE, EXTENDED_PROPERTY_MOUNT_TYPE));
        props.add(GccNCPreview.NAME);
        props.add(new Property.Name(NC_NAMESPACE, EXTENDED_PROPERTY_NOTE));

        return props.toArray(new Property.Name[0]);
    }

    public static void registerCustomFactories() {
        PropertyRegistry propertyRegistry = PropertyRegistry.INSTANCE;

        propertyRegistry.register(new GccOCId.Factory());
        propertyRegistry.register(new GccNCPreview.Factory());
        propertyRegistry.register(new GccNCEncrypted.Factory());
        propertyRegistry.register(new GccOCFavorite.Factory());
        propertyRegistry.register(new GccOCSize.Factory());
        propertyRegistry.register(new GccNCPermission.Factory());
    }
}
