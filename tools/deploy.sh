#!/bin/bash

rsync -avz --delete-during --delete-excluded --exclude work --exclude final --exclude '.*.sw?' . syn:~/.www/miskinhill.com.au/content/
