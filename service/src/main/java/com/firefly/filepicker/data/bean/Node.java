package com.firefly.filepicker.data.bean;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Created by rany on 18-1-9.
 *
 *   Type:
 *               root:                        ROOT
 *                                          /  \   \
 *                                         /   \    \
 *              category:            Local    DLNA  samba
 *                                     /
 *                                    /    ....
 *              device:              0
 *                                  /
 *                                 /       ...
 *             file:            Music
 *
 */

public class Node implements Serializable {
    @IntDef({ROOT,
            INTERNAL,
            EXTERNAL,
            DLNA,
            SAMBA,
            INTERNAL_CATEGORY,
            EXTERNAL_CATEGORY,
            DLNA_CATEGORY,
            SAMBA_CATEGORY,
            INTERNAL_DEVICE,
            EXTERNAL_DEVICE,
            DLNA_DEVICE,
            SAMBA_DEVICE})
    public @interface Type {};
    public static final int ROOT = -1;

    public static final int INTERNAL = 0;
    public static final int EXTERNAL = 1;
    public static final int DLNA = 2;
    public static final int SAMBA = 3;

    public static final int INTERNAL_CATEGORY = 4;
    public static final int EXTERNAL_CATEGORY = 5;
    public static final int DLNA_CATEGORY = 6;
    public static final int SAMBA_CATEGORY = 7;

    public static final int INTERNAL_DEVICE = 8;
    public static final int EXTERNAL_DEVICE = 9;
    public static final int DLNA_DEVICE = 10;
    public static final int SAMBA_DEVICE = 11;

    private String id;
    private String title;
    private int type;

    private Object item;
    private Node parent;
    private List<Node> children;

    private final int flag = new Random().nextInt();

    public Node(@NonNull String id, String title, int type, Object item) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.item = item;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Type
    public int getType() {
        return type;
    }

    public void setType(@Type int type) {
        this.type = type;
    }

    public Object getItem() {
        return item;
    }

    public void setItem(Object item) {
        this.item = item;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public List<Node> getChildren() {
        return children;
    }

    public void setChildren(List<Node> children) {
        this.children = children;
    }

    public void addChild(Node child) {
        if (child.getParent() != null) {
            return;
        }

        if (children == null) {
            children = new ArrayList<>();
        }

        child.setParent(this);
        children.add(child);
    }

    public void removeChild(Node node) {
        if (children.indexOf(node) != -1) {
            children.remove(node);
        }
    }

    public Node getChild(int position) {
        if (children != null && position < children.size() && position >= 0) {
            return children.get(position);
        }

        return null;
    }

    public Node getChild(@NonNull String id) {
        return getChild(this, id);
    }

    private Node getChild(Node root, String id) {
        if (!hasChild()) {
            return null;
        }

        for (Node node : root.getChildren()) {
            if (id.equals(node.getId())) {
                return node;
            } else {
                Node n = getChild(node, id);
                if (n != null) {
                    return n;
                }
            }
        }

        return null;
    }

    public boolean replaceChild(Node root, Node newNode) {
        if (!hasChild()) {
            return false;
        }

        if (root.equals(newNode)) {
            root.setTitle(newNode.getTitle());
            root.setItem(newNode.getItem());
            root.setChildren(newNode.getChildren());
            return true;
        }

        if (root.hasChild()) {
            for (Node node : root.getChildren()) {
                if (replaceChild(node, newNode)) {
                    return true;
                }
            }
        }


        return false;
    }

    public boolean hasChild() {
        return children != null && !children.isEmpty();
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Node) {
            Node node = (Node) object;

            return Objects.equals(node.getId(), this.getId());
        }

        return false;
    }

    public boolean isCategory() {
        return DLNA_CATEGORY == type
                || SAMBA_CATEGORY == type
                || EXTERNAL_CATEGORY == type
                || INTERNAL_CATEGORY == type;
    }

    public boolean isDevice() {
        return DLNA_DEVICE == type
                || SAMBA_DEVICE == type
                || EXTERNAL_DEVICE == type
                || INTERNAL_DEVICE == type;
    }

    public int getFlag() {
        return flag;
    }
}
