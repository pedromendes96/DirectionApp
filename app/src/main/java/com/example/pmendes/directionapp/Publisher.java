package com.example.pmendes.directionapp;

public interface Publisher {
    void Add(Subscriber subscriber);
    void Remove(Subscriber subscriber);
    void Publish(OrientationAnalyzer.DIRECTION direction);
}
