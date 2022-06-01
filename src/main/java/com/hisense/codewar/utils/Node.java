package com.hisense.codewar.utils;

//结点的属性
//因为每个结点都需要存放在优先队列中，所以需要实现Comparable接口
public class Node implements Comparable<Node> {
    public int x;  //x坐标
    public int y;  //y坐标
    public int F;  //F属性
    public int G;  //G属性
    public int H;  //H属性
    public Node Father;    //此结点的上一个结点
    //构造函数
    public Node(int x, int y) {
        this.x = x;
        this.y = y;
    }
    //通过结点的坐标和目标结点的坐标可以计算出F， G， H三个属性
    //需要传入这个节点的上一个节点和最终的结点
    public void init_node(Node father, Node end) {
        this.Father = father;
        if (this.Father != null) {
            //走过的步数等于父节点走过的步数加一
            this.G = father.G + 1;
        } else { //父节点为空代表它是第一个结点
            this.G = 0;
        }
        //计算通过现在的结点的位置和最终结点的位置计算H值
        this.H = Math.abs(this.x - end.x) + Math.abs(this.y - end.y);
        this.F = this.G + this.H;
    }
    // 用来进行和其他的Node类进行比较重写的方法
    @Override
    public int compareTo(Node o) {
        return Integer.compare(this.F, o.F);
    }
 }
 
