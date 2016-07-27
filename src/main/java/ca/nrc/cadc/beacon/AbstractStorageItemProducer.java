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

package ca.nrc.cadc.beacon;

import ca.nrc.cadc.beacon.web.StorageItemFactory;
import ca.nrc.cadc.net.NetUtil;
import ca.nrc.cadc.reg.client.RegistryClient;
import ca.nrc.cadc.vos.ContainerNode;
import ca.nrc.cadc.vos.Node;
import ca.nrc.cadc.vos.VOSURI;
import ca.nrc.cadc.vos.client.VOSpaceClient;

import javax.security.auth.Subject;
import java.net.URI;
import java.security.PrivilegedExceptionAction;
import java.util.List;


public abstract class AbstractStorageItemProducer<T extends StorageItemWriter>
        implements StorageItemProducer
{
    int pageCount = 0;
    VOSURI current;
    final int pageSize;
    final VOSURI folderURI;
    final Subject user;
    final T storageItemWriter;
    final StorageItemFactory storageItemFactory;


    public AbstractStorageItemProducer(int pageSize, VOSURI folderURI,
                                       final VOSURI startURI,
                                       final T storageItemWriter,
                                       final Subject user,
                                       final StorageItemFactory storageItemFactory)
    {
        this.pageSize = pageSize;
        this.folderURI = folderURI;
        this.current = startURI;
        this.storageItemWriter = storageItemWriter;
        this.user = user;
        this.storageItemFactory = storageItemFactory;
    }


    String getQuery()
    {
        return "detail=max&limit=" + ((pageSize > 0) ? pageSize : 300)
               + ((current == null)
                  ? "" : "&uri=" + NetUtil.encode(current.toString()));
    }

    List<Node> nextPage() throws Exception
    {
        final RegistryClient registryClient = new RegistryClient();
        final VOSpaceClient voSpaceClient =
                new VOSpaceClient(registryClient.getServiceURL(
                        URI.create("ivo://cadc.nrc.ca/vospace"),
                        "http").toExternalForm(), false);

        final List<Node> nodes =
                Subject.doAs(user, (PrivilegedExceptionAction<List<Node>>) () ->
                        ((ContainerNode) voSpaceClient.getNode(
                                folderURI.getPath(), getQuery())).getNodes());

        return (current == null) ? nodes : (nodes.size() > 1)
                                           ? nodes.subList(1, nodes.size())
                                           : null;
    }

    boolean writePage(final List<Node> page) throws Exception
    {
        if (page == null)
        {
            return false;
        }
        else
        {
            for (final Node n : page)
            {
                this.storageItemWriter.write(storageItemFactory.translate(n));
                this.current = n.getUri();
            }

            return true;
        }
    }

    @Override
    public VOSURI getLastWrittenURI()
    {
        return this.current;
    }

    @Override
    public boolean writePage() throws Exception
    {
        return writePage(nextPage());
    }

    protected void writePages() throws Exception
    {
        while (writePage())
        {
            pageCount++;
        }
    }
}
