#!/bin/bash

rsync -av rdf.nt syn:~/.www/miskinhill.com.au/
rsync -av --delete-during --delete-excluded --exclude genshi --exclude Genshi.egg-info --exclude '*.pyc' --exclude '.*.sw?' web/ syn:~/.www/miskinhill.com.au/app/
rsync -av --delete-during --delete-excluded --exclude work --exclude final --exclude '.*.sw?' content syn:~/.www/miskinhill.com.au/
