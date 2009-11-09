#!/bin/bash

rsync -avz --delete-during --delete-excluded --exclude work --exclude final --exclude .hg --exclude '.*.sw?' . syn:~/.www/miskinhill.com.au/content/
rsync -avz --delete-during --delete-excluded --exclude work --exclude final --exclude .hg --exclude '.*.sw?' . miskinhill:/var/www/miskinhill.com.au/content/
