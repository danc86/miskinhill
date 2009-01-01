#!/bin/bash

rsync -av --delete-during --delete-excluded --exclude work --exclude final --exclude '.*.sw?' . syn:~/.www/miskinhill.com.au/content/
