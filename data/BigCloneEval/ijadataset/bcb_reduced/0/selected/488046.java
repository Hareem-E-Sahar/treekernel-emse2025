package resilience;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import resilience.interfaces.ISaveable;

/**
 * @version $Id: Saveable.java 174 2011-09-12 08:28:23Z pespie $
 * @since 1.0
 * @author Patrice Espie
 * @contact <a href='mailto:patrice.espie@gmail.com'>patrice.espie@gmail.com</a>
 * @licensing <i>Released under the LGPL. This is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General
 *            Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later
 *            version.</i>
 *            <p>
 *            <i>This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 *            MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.</i>
 *            <p>
 *            <i>You should have received a copy of the GNU Lesser General Public License along with this software; if not, write to the Free Software
 *            Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF site: <a
 *            href='http://www.fsf.org'>http://www.fsf.org</a>.</i>
 */
public abstract class Saveable implements ISaveable {

    private transient ReentrantReadWriteLock readWriteLock;

    protected Saveable() {
        initTransientFields();
    }

    @Override
    public void beginRead() {
        readWriteLock.readLock().lock();
    }

    @Override
    public void beginWrite() {
        readWriteLock.writeLock().lock();
    }

    @Override
    public void endRead() {
        readWriteLock.readLock().unlock();
    }

    @Override
    public void endWrite() {
        readWriteLock.writeLock().unlock();
    }

    protected Object readResolve() {
        initTransientFields();
        return this;
    }

    private void initTransientFields() {
        readWriteLock = new ReentrantReadWriteLock(true);
    }
}
