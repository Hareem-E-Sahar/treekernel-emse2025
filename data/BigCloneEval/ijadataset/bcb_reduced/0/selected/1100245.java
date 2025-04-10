package desmoj.core.simulator;

import java.util.Iterator;
import org.apache.commons.collections.list.TreeList;
import desmoj.core.exception.SimAbortedException;
import desmoj.core.report.ErrorMessage;

/**
 * Alternative Implementation of the interface <code>EventList</code> using a
 * tree-based list as a container for the event-notes, yielding both access and
 * removal of event-list entries in O(log n) time.
 * 
 * Disadvantages compared to <code>EventVector</code> include
 * non-thread-safeness (however, discrete Event simulation should never attempt
 * concurrent modifications of the event-list) and the slightly higher memory
 * requirement.
 * 
 * The internal tree-based list is provided by the class
 * <code>org.apache.commons.collections.list.TreeList</code> from the Commons
 * Collections package from the Apache Jakarta Commons Project (see
 * http://jakarta.apache.org/commons/index.html). Thus, his product includes
 * software developed by The Apache Software Foundation
 * (http://www.apache.org/). For License see
 * http://www.apache.org/licenses/LICENSE-2.0 (of which a copy can be found in
 * the root directory of this distribtuon).
 * 
 * @see org.apache.commons.collections.list.TreeList
 * @see EventVectorList
 * @see EventNote
 * 
 * @version DESMO-J, Ver. 2.3.4beta copyright (c) 2012
 * @author Tim Lechler, Ruth Meyer, modified by Johannes Göbel
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */
public class EventTreeList extends EventList {

    /**
	 * The tree list container used to store the event-notes.
	 */
    TreeList eTreeList;

    /**
	 * Constructs an empty event-list.
	 */
    EventTreeList() {
        eTreeList = new TreeList();
    }

    /**
	 * Creates a new event-note with the initial values given as parameters. This
	 * resembles the factory method design pattern described in [Gamm95] p. 107.
	 * This design pattern is used to ensure that always the appropriate
	 * implementation of Event notes is used together with an individual
	 * implementation of an event-list. EventTreeList does not need any special
	 * implementation of Event notes and thus simply passes the construction
	 * through to the default implementation of EventNote.
	 * 
	 * @param who
	 *            Entity : the entity or process associated with the event-note
	 * @param what
	 *            Event : the event or external event associated with the
	 *            EventNote
	 * @param when
	 *            TimeInstant : the point in simulation time associated with the
	 *            EventNote
	 * @see EventNote
	 */
    EventNote createEventNote(Entity who, Event<Entity> what, TimeInstant when) {
        return new EventNote(who, what, when);
    }

    /**
	 * Returns the first event-note in the event-list. It is the event-note with
	 * the lowest (nearest) associated point of simulation time of all
	 * Event notes contained in the evnet-list. Note that the event-note is not
	 * removed from the event-list.
	 * 
	 * @return EventNote : the event-note to be processed next in the order of
	 *         time. Returns <code>null</code> if the event-list is empty.
	 */
    EventNote firstNote() {
        if (isEmpty()) return null; else return (EventNote) eTreeList.get(0);
    }

    /**
	 * Inserts the new event-note preserving the temporal order of the event-notes
	 * contained in the event-list. It uses binary search to determine the
	 * position where to insert the new event-note to increase performance.
	 * 
	 * @param newNote
	 *            EventNote : the new note to be inserted in the event-list
	 *            keeping the temporal order
	 */
    void insert(EventNote newNote) {
        Entity who1 = newNote.getEntity1();
        if (who1 != null) {
            who1.addEventNote(newNote);
        }
        Entity who2 = newNote.getEntity2();
        if (who2 != null) {
            who2.addEventNote(newNote);
        }
        Entity who3 = newNote.getEntity3();
        if (who3 != null) {
            who3.addEventNote(newNote);
        }
        EventAbstract Event = newNote.getEvent();
        if (Event != null) {
            Event.addEventNote(newNote);
        }
        if (isEmpty()) {
            eTreeList.add(newNote);
            return;
        } else {
            int left = 0;
            int right = eTreeList.size() - 1;
            int index = 0;
            TimeInstant refTime = newNote.getTime();
            do {
                index = (left + right) / 2;
                if (TimeInstant.isBeforeOrEqual(((EventNote) eTreeList.get(index)).getTime(), refTime)) {
                    if (index < (eTreeList.size() - 1)) {
                        if (TimeInstant.isAfter(((EventNote) eTreeList.get(index + 1)).getTime(), refTime)) {
                            eTreeList.add(index + 1, newNote);
                            return;
                        } else {
                            left = index + 1;
                        }
                    } else {
                        eTreeList.add(newNote);
                        return;
                    }
                } else {
                    if (index > 0) {
                        if (TimeInstant.isBeforeOrEqual(((EventNote) eTreeList.get(index - 1)).getTime(), refTime)) {
                            eTreeList.add(index, newNote);
                            return;
                        } else {
                            right = index - 1;
                        }
                    } else {
                        eTreeList.add(0, newNote);
                        return;
                    }
                }
            } while ((left <= right));
            eTreeList.add(newNote);
        }
    }

    /**
	 * Inserts a new event-note after another EventNote specified. Note that to
	 * keep the temporal order of the event-list, the scheduled time will be set
	 * to the same time as the referred "afterNote". Note also, that afterNote
	 * must be contained in the event-list. If the referred "where" is not
	 * contained in the event-list, there is no chance to determine the time
	 * that the new note is intended to be scheduled at. Thus the new event-note
	 * will not be inserted and a <code>EventNotScheduledException</code> will
	 * be thrown, stopping the simulation.
	 * 
	 * @param where
	 *            EventNote : The event-note containing the event after which the
	 *            new note is supposed to be inserted into the event-list.
	 * @param newNote
	 *            EventNote : The new event-note to be inserted after the
	 *            specified EventNote in the event-list.
	 * @throws SimAbortedException
	 *                : if referred EventNote is not contained in the event-list
	 */
    void insertAfter(EventNote where, EventNote newNote) {
        Entity who1 = newNote.getEntity1();
        if (who1 != null) {
            who1.addEventNote(newNote);
        }
        Entity who2 = newNote.getEntity2();
        if (who2 != null) {
            who2.addEventNote(newNote);
        }
        Entity who3 = newNote.getEntity3();
        if (who3 != null) {
            who3.addEventNote(newNote);
        }
        EventAbstract Event = newNote.getEvent();
        if (Event != null) {
            Event.addEventNote(newNote);
        }
        int i = eTreeList.indexOf(where);
        if (i < 0) {
            Model mBuffer = null;
            if (newNote.getEntity1() != null) {
                mBuffer = newNote.getEntity1().getModel();
            }
            if (newNote.getEvent() != null) {
                mBuffer = newNote.getEvent().getModel();
            }
            throw new SimAbortedException(new ErrorMessage(mBuffer, "Can not insert new event-note after given EventNote! " + "Simulation aborted", "Internal DESMO-J class : EventTreeList Method : " + "insertAfter(EventNote where, EventNote newNote)", "The event-note to insert the new note after is not contained " + "in the event tree list.", "This is a fatal error. Contact DESMOJ support", newNote.getTime()));
        } else {
            newNote.setTime(where.getTime());
            eTreeList.add(i + 1, newNote);
        }
    }

    /**
	 * Inserts the given EventNote at the first position in the event-list. The
	 * Event encapsulated in that EventNote will probably be the next event to
	 * be processed by the scheduler (unless some other calls to this method are
	 * made before). Note that for consistency the time of the new event-note 
	 * is set to the time of the next entry, if the time of the next entry is earlier. 
	 * 
	 * @param newNote
	 *            EventNote : The event-note to be inserted at the first position
	 *            in the event-list.
	 */
    void insertAsFirst(EventNote newNote) {
        if (!isEmpty()) {
            TimeInstant next = ((EventNote) eTreeList.get(0)).getTime();
            if (TimeInstant.isBefore(next, newNote.getTime())) {
                newNote.setTime(next);
            }
        }
        eTreeList.add(0, newNote);
        Entity who1 = newNote.getEntity1();
        if (who1 != null) {
            who1.addEventNote(newNote);
        }
        Entity who2 = newNote.getEntity2();
        if (who2 != null) {
            who2.addEventNote(newNote);
        }
        Entity who3 = newNote.getEntity3();
        if (who3 != null) {
            who3.addEventNote(newNote);
        }
        EventAbstract Event = newNote.getEvent();
        if (Event != null) {
            Event.addEventNote(newNote);
        }
    }

    /**
	 * Inserts a new event-note before another EventNote specified. Note that
	 * this could disturb the temporal order of the event-list. So this method
	 * should only be used carefully. Note also, that EventNote 'where' must be
	 * contained in the event-list or otherwise an exception will be thrown.
	 * 
	 * @param where
	 *            EventNote : The event-note containing the event before which
	 *            the newNote is supposed to be inserted into the event-list.
	 * @param newNote
	 *            EventNote : The new event-note to be inserted before the
	 *            specified EventNote in the event-list
	 * @throws SimAbortedException
	 *                : if referred EventNote is not contained in the event-list
	 */
    void insertBefore(EventNote where, EventNote newNote) {
        Entity who1 = newNote.getEntity1();
        if (who1 != null) {
            who1.addEventNote(newNote);
        }
        Entity who2 = newNote.getEntity2();
        if (who2 != null) {
            who2.addEventNote(newNote);
        }
        Entity who3 = newNote.getEntity3();
        if (who3 != null) {
            who3.addEventNote(newNote);
        }
        EventAbstract Event = newNote.getEvent();
        if (Event != null) {
            Event.addEventNote(newNote);
        }
        int i = eTreeList.indexOf(where);
        if (i < 0) {
            Model mBuffer = null;
            if (newNote.getEntity1() != null) {
                mBuffer = newNote.getEntity1().getModel();
            }
            if (newNote.getEvent() != null) {
                mBuffer = newNote.getEvent().getModel();
            }
            throw new SimAbortedException(new ErrorMessage(mBuffer, "Can not insert new event-note before given EventNote! " + "Simulation aborted", "Internal DESMO-J class : EventTreeList Method : " + "insertBefore(EventNote where, EventNote newNote)", "The event-note to insert the new note before is not contained " + "in the event tree list.", "This is a fatal error. Contact DESMOJ support", newNote.getTime()));
        } else {
            newNote.setTime(where.getTime());
            eTreeList.add(i, newNote);
        }
    }

    /**
	 * Tests if there are any scheduled events contained in the event-list. If
	 * the event-list happens to be empty during the run of a simulation, this
	 * is a criterium to stop the simulation, since no further action is
	 * scheduled.
	 * 
	 * @return boolean : True if there are no Event notes contained in the
	 *         event-list, false otherwise.
	 */
    boolean isEmpty() {
        return eTreeList.isEmpty();
    }

    /**
	 * Returns the last EventNote in the event-list. If the event-list is empty,
	 * <code>null</code> will be returned.
	 * 
	 * @return EventNote : the last EventNote in the event-list, null if the
	 *         event-list is empty
	 */
    EventNote lastNote() {
        if (isEmpty()) return null; else return (EventNote) eTreeList.get(eTreeList.size() - 1);
    }

    /**
	 * Returns the next event-note in the event-list relative to the given
	 * EventNote. If the given EventNote is not contained in the event-list or
	 * happens to be the last EventNote in the event-list, null will be
	 * returned.
	 * 
	 * @return EventNote : The event-note following the given EventNote or
	 *         <ocde>null</code> if the given EventNote was last or not found
	 * @param origin
	 *            EventNote : The event-note whose successor is wanted
	 */
    EventNote nextNote(EventNote origin) {
        if (eTreeList.contains(origin)) {
            if (origin == eTreeList.get(eTreeList.size() - 1)) {
                return null;
            } else return (EventNote) eTreeList.get(eTreeList.indexOf(origin) + 1);
        }
        return null;
    }

    /**
	 * Returns the previous EventNote in the event-list relative to the given
	 * EventNote. If the given EventNote is not contained in the event-list or
	 * happens to be the first event-note in the event-list, null will be
	 * returned.
	 * 
	 * @return EventNote : The event-note following the given EventNote or
	 *         <ocde>null</code> if the given EventNote was first or not found
	 * @param origin
	 *            EventNote : The event-note whose predecessor is wanted
	 */
    EventNote prevNote(EventNote origin) {
        if (eTreeList.contains(origin)) {
            if (origin == eTreeList.get(0)) {
                return null;
            }
            return (EventNote) eTreeList.get(eTreeList.indexOf(origin) - 1);
        }
        return null;
    }

    /**
	 * Removes the given EventNote from the event-list.
	 * 
	 * Warning: Make sure to tell the entity of the event-note to delete
	 * the Note from its List as well.
	 * 
	 * Warning: Make sure to tell the entity of the event-note to delete
	 * the Note from its List as well.
	 * 
	 * @param note
	 *            EventNote : The event-note to be removed from the event-list
	 */
    void remove(EventNote note) {
        if (!eTreeList.contains(note)) return; else {
            eTreeList.remove(note);
            if (note.getEntity1() != null) {
                note.getEntity1().removeEventNote(note);
            }
            if (note.getEntity2() != null) {
                note.getEntity2().removeEventNote(note);
            }
            if (note.getEntity3() != null) {
                note.getEntity3().removeEventNote(note);
            }
            if (note.getEvent() != null) {
                note.getEvent().removeEventNote(note);
            }
        }
    }

    /**
	 * Removes the first event-note from the event-list. Does nothing if the
	 * event-list is already empty.
	 */
    void removeFirst() {
        if (!eTreeList.isEmpty()) {
            EventNote note = firstNote();
            eTreeList.remove(0);
            if (note.getEntity1() != null) {
                note.getEntity1().removeEventNote(note);
            }
            if (note.getEntity2() != null) {
                note.getEntity2().removeEventNote(note);
            }
            if (note.getEntity3() != null) {
                note.getEntity3().removeEventNote(note);
            }
            if (note.getEvent() != null) {
                note.getEvent().removeEventNote(note);
            }
        }
    }

    /**
	 * Returns a string representing the entries of this tree list in a row. The
	 * resulting string includes all Event notes in ascending order as they are
	 * placed inside the event tree list.
	 */
    public String toString() {
        StringBuffer textBuffer = new StringBuffer();
        Iterator<?> notes = eTreeList.iterator();
        while (notes.hasNext()) {
            textBuffer.append("[");
            textBuffer.append((EventNote) notes.next());
            textBuffer.append("]");
        }
        return textBuffer.toString();
    }
}
