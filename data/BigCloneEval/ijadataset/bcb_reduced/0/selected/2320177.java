package com.incendiaryblue.util;

import java.lang.reflect.*;

/**
 *	Provides a mechanism for mutable-size arrays. This class allows the user to
 *	declare and access an array directly, whilst the class manages the on-demand
 *	resizing of the array when additions to it and removals from it are made.
 *	This has the advantage over lists provided by the Collection API of
 *	speed since the original array is owned and directly accessed by the user.
 *	<P>
 *	To create a mutable array:<P>
 *	<UL>
 *		<LI>Declare the array.
 *		<LI>Declare an instance of the mutable array manager passing the array
 *		to it.
 *		<LI>Make modifications to the array only by calling methods on the
 *		mutable array manager, and making sure the declared array is reassigned
 *		the modified array returned by the called modification method.
 *	</UL>
 *	<P>
 *	Example:<P>
 *	<CODE>
 *	private Node nodeList = new Node[10];
 *
 *	private MutableArrayManager arrayManager =
 *		new MutableArrayManager(nodeList);
 *
 *		.
 *		.
 *		.
 *
 *	public void addNode(Node newNode)
 *	{
 *		this.nodeList = (Node[]) this.arrayManager.add(newNode);
 *	}
 *
 *	</CODE>
 */
public class MutableArrayManager {

    /**
	 *	The array being managed.
	 */
    private Object[] array;

    /**
	 *	The increment made to the length of the associated array when its
	 *	capacity it is at capacity and a further addition must be made.
	 */
    private int capacityIncrement;

    /**
	 *	The number of items currently in the associated array.
	 */
    private int length;

    /**
	 *	Constructs a new mutable array manager that manages the given array.
	 *	The capacity increment specified by DEFAULT_CAPACITY_INCREMENT will
	 *	be used when an addition is requested and the current array is at
	 *	capacity.
	 *
	 *	@param	array	The initial array to be managed. 
	 */
    public MutableArrayManager(Object[] array) {
        this.array = array;
        this.capacityIncrement = array.length;
    }

    /**
	 *	Adds a new object to the array being managed.<P>
	 *
	 *	Typically, the user should assign the return value of this method
	 *	straight to the variable referencing the original array given to the
	 *	constructor of this object.
	 *
	 *	@param	newObject	The object to be added to the array.
	 *
	 *	@return	The array being managed by this object, which may or may not
	 *			have been reallocated.
	 */
    public Object[] add(Object newObject) {
        if (this.length == this.array.length) {
            Class arrayClass = this.array.getClass();
            this.array = allocateNewArray(this.length + this.capacityIncrement);
        }
        this.array[this.length++] = newObject;
        return this.array;
    }

    private Object[] allocateNewArray(int capacity) {
        Class arrayClass = this.array.getClass();
        Object[] newArray = (Object[]) Array.newInstance(arrayClass.getComponentType(), capacity);
        System.arraycopy(this.array, 0, newArray, 0, this.length);
        return newArray;
    }

    public Object[] remove(Object object) {
        int indexOfObject = -1;
        for (int i = 0; (i < this.length) && (indexOfObject == -1); i++) {
            if (this.array[i] == object) {
                indexOfObject = i;
            }
        }
        if (indexOfObject == -1) {
            return this.array;
        }
        int numElementsToMove = this.length - indexOfObject - 1;
        System.arraycopy(this.array, indexOfObject + 1, this.array, indexOfObject, numElementsToMove);
        this.length--;
        if (this.length % this.capacityIncrement == 0) {
            this.array = allocateNewArray(this.length);
        }
        return this.array;
    }

    /**
	 *	@return	The number of items in the array being managed by this object.
	 */
    public int getLength() {
        return this.length;
    }
}
