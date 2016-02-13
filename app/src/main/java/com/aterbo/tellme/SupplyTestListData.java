package com.aterbo.tellme;

import com.aterbo.tellme.classes.ConvoToHear;
import com.aterbo.tellme.classes.ConvoToTell;
import com.aterbo.tellme.classes.ConvoToWaitFor;

import java.util.ArrayList;

/**
 * Created by ATerbo on 2/12/16.
 */
public class SupplyTestListData {

    public SupplyTestListData(){}

    public ArrayList<ConvoToTell> getTestConvoToTell(){
        ArrayList<ConvoToTell> testList = new ArrayList<>();

        testList.add(new ConvoToTell(true));
        testList.add(new ConvoToTell(true));
        testList.add(new ConvoToTell(true));

        return testList;
    }

    public ArrayList<ConvoToHear> getTestConvoToHear(){
        ArrayList<ConvoToHear> testList = new ArrayList<>();

        testList.add(new ConvoToHear(true));
        testList.add(new ConvoToHear(true));
        testList.add(new ConvoToHear(true));
        testList.add(new ConvoToHear(true));

        return testList;
    }

    public ArrayList<ConvoToWaitFor> getTestConvoToWaitFor(){
        ArrayList<ConvoToWaitFor> testList = new ArrayList<>();


        testList.add(new ConvoToWaitFor(true));
        testList.add(new ConvoToWaitFor(true));
        testList.add(new ConvoToWaitFor(true));
        testList.add(new ConvoToWaitFor(true));
        testList.add(new ConvoToWaitFor(true));
        testList.add(new ConvoToWaitFor(true));

        return testList;
    }
}
