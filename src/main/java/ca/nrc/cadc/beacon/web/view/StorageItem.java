/*
 ************************************************************************
 *******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
 **************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
 *
 *  (c) 2016.                            (c) 2016.
 *  Government of Canada                 Gouvernement du Canada
 *  National Research Council            Conseil national de recherches
 *  Ottawa, Canada, K1A 0R6              Ottawa, Canada, K1A 0R6
 *  All rights reserved                  Tous droits réservés
 *
 *  NRC disclaims any warranties,        Le CNRC dénie toute garantie
 *  expressed, implied, or               énoncée, implicite ou légale,
 *  statutory, of any kind with          de quelque nature que ce
 *  respect to the software,             soit, concernant le logiciel,
 *  including without limitation         y compris sans restriction
 *  any warranty of merchantability      toute garantie de valeur
 *  or fitness for a particular          marchande ou de pertinence
 *  purpose. NRC shall not be            pour un usage particulier.
 *  liable in any event for any          Le CNRC ne pourra en aucun cas
 *  damages, whether direct or           être tenu responsable de tout
 *  indirect, special or general,        dommage, direct ou indirect,
 *  consequential or incidental,         particulier ou général,
 *  arising from the use of the          accessoire ou fortuit, résultant
 *  software.  Neither the name          de l'utilisation du logiciel. Ni
 *  of the National Research             le nom du Conseil National de
 *  Council of Canada nor the            Recherches du Canada ni les noms
 *  names of its contributors may        de ses  participants ne peuvent
 *  be used to endorse or promote        être utilisés pour approuver ou
 *  products derived from this           promouvoir les produits dérivés
 *  software without specific prior      de ce logiciel sans autorisation
 *  written permission.                  préalable et particulière
 *                                       par écrit.
 *
 *  This file is part of the             Ce fichier fait partie du projet
 *  OpenCADC project.                    OpenCADC.
 *
 *  OpenCADC is free software:           OpenCADC est un logiciel libre ;
 *  you can redistribute it and/or       vous pouvez le redistribuer ou le
 *  modify it under the terms of         modifier suivant les termes de
 *  the GNU Affero General Public        la “GNU Affero General Public
 *  License as published by the          License” telle que publiée
 *  Free Software Foundation,            par la Free Software Foundation
 *  either version 3 of the              : soit la version 3 de cette
 *  License, or (at your option)         licence, soit (à votre gré)
 *  any later version.                   toute version ultérieure.
 *
 *  OpenCADC is distributed in the       OpenCADC est distribué
 *  hope that it will be useful,         dans l’espoir qu’il vous
 *  but WITHOUT ANY WARRANTY;            sera utile, mais SANS AUCUNE
 *  without even the implied             GARANTIE : sans même la garantie
 *  warranty of MERCHANTABILITY          implicite de COMMERCIALISABILITÉ
 *  or FITNESS FOR A PARTICULAR          ni d’ADÉQUATION À UN OBJECTIF
 *  PURPOSE.  See the GNU Affero         PARTICULIER. Consultez la Licence
 *  General Public License for           Générale Publique GNU Affero
 *  more details.                        pour plus de détails.
 *
 *  You should have received             Vous devriez avoir reçu une
 *  a copy of the GNU Affero             copie de la Licence Générale
 *  General Public License along         Publique GNU Affero avec
 *  with OpenCADC.  If not, see          OpenCADC ; si ce n’est
 *  <http://www.gnu.org/licenses/>.      pas le cas, consultez :
 *                                       <http://www.gnu.org/licenses/>.
 *
 *
 ************************************************************************
 */

package ca.nrc.cadc.beacon.web.view;

import ca.nrc.cadc.beacon.FileSizeRepresentation;
import ca.nrc.cadc.date.DateUtil;
import ca.nrc.cadc.vos.VOSURI;

import java.net.URI;
import java.text.DateFormat;
import java.util.Date;


public abstract class StorageItem
{
    private static final FileSizeRepresentation FILE_SIZE_REPRESENTATION =
            new FileSizeRepresentation();
    private static final DateFormat DATE_FORMAT =
            DateUtil.getDateFormat("yyyy-MM-dd ' - ' HH:mm:ss", DateUtil.UTC);
    static final String NO_SIZE_DISPLAY = "--";

    private final String name;
    private final long sizeInBytes;
    private final Date lastModified;
    private final URI[] writeGroupURIs;
    private final URI[] readGroupURIs;
    private final String owner;

    final VOSURI uri;
    final boolean publicFlag;
    final boolean lockedFlag;


    public StorageItem(VOSURI uri, long sizeInBytes, Date lastModified,
                       boolean publicFlag, boolean lockedFlag,
                       URI[] writeGroupURIs, URI[] readGroupURIs,
                       final String owner)
    {
        this(uri, uri.getName(), sizeInBytes, lastModified, publicFlag,
             lockedFlag, writeGroupURIs, readGroupURIs, owner);
    }


    public StorageItem(VOSURI uri, String name, long sizeInBytes,
                       Date lastModified, boolean publicFlag,
                       boolean lockedFlag, URI[] writeGroupURIs,
                       URI[] readGroupURIs, final String owner)
    {
        this.uri = uri;
        this.name = name;
        this.sizeInBytes = sizeInBytes;
        this.lastModified = lastModified;
        this.publicFlag = publicFlag;
        this.lockedFlag = lockedFlag;
        this.writeGroupURIs = writeGroupURIs;
        this.readGroupURIs = readGroupURIs;
        this.owner = owner;
    }

    public String getSizeHumanReadable()
    {
        return FILE_SIZE_REPRESENTATION.getSizeHumanReadable(sizeInBytes);
    }

    public long getSizeInBytes()
    {
        return sizeInBytes;
    }

    public String getLastModifiedHumanReadable()
    {
        return DATE_FORMAT.format(lastModified);
    }

    public String getPath()
    {
        return uri.getPath();
    }

    public String getName()
    {
        return name;
    }

    public VOSURI getURI() { return uri; }

    public boolean isPublic()
    {
        return publicFlag;
    }

    public boolean isLocked()
    {
        return lockedFlag;
    }

    public String getParentPath()
    {
        return uri.isRoot() ? "/" : uri.getParent();
    }

    public String getWriteGroupNames()
    {
        return getURINames(this.writeGroupURIs);
    }

    public String getReadGroupNames()
    {
        return getURINames(this.readGroupURIs);
    }

    public String getOwner()
    {
        return owner;
    }

    public String getURINames(final URI[] uris)
    {
        final String[] names = new String[uris.length];
        final int urisLength = uris.length;

        for (int i = 0; i < urisLength; i++)
        {
            names[i] = uris[i].getFragment();
        }

        return String.join(" ", names);
    }

    public abstract String getItemIconCSS();

    public abstract String getLinkURI();
}
