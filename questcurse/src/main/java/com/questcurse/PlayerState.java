package com.questcurse;

import java.util.ArrayList;
import java.util.List;

/** Состояние игрока. Все таймеры — в секундах и уменьшаются раз в секунду. */
public class PlayerState {
    public int untilNextSec;        // секунд до следующего задания
    public TaskDef task;            // активное задание
    public int taskLeftSec;         // секунд до провала
    public int baseline;            // сколько предметов было на старте (COLLECT)
    public int progress;
    public TaskDef lastTask;        // чтобы не выдавать одно и то же подряд
    public final List<Curse> curses = new ArrayList<>();

    public static class Curse {
        public final BodyPart part;
        public int leftSec;
        public Curse(BodyPart part, int leftSec) { this.part = part; this.leftSec = leftSec; }
    }
}
