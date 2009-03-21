
"""
Converts figleaf coverage results from figleaf.read_coverage(...) or 
figleaf.get_info() to a Clover-compatible XML report.
"""

import os, re, time, datetime, warnings
import figleaf

def to_relative(path):
    path = os.path.realpath(path)
    cwd = os.path.realpath(os.getcwd()) + '/'
    if path.startswith(cwd):
        return path[len(cwd):]
    else:
        return path

def convert(coverage, exclude_pattern, out, package_name):
    results = {} # included filename -> (n_covered, n_interesting, n_all)
    for src_file, covered_lines in coverage.iteritems():
        src_file = to_relative(src_file)
        if re.match(exclude_pattern, src_file): continue
        try:
            src_lines = figleaf.get_lines(open(src_file, 'rU'))
        except IOError, e:
            if e.errno == 2: # probably something crap like <string>
                warnings.warn('skipping unreadable source file %r' % src_file)
                continue
            else:
                raise
        n_covered = n_interesting = n_all = 0
        for i, line in enumerate(open(src_file, 'rU')):
            i += 1 # line numbers are 1-indexed
            if i in covered_lines:
                n_covered += 1
                n_interesting += 1
            elif i in src_lines:
                n_interesting += 1
            n_all += 1
        results[src_file] = (n_covered, n_interesting, n_all)

    stamp = time.mktime(datetime.datetime.now().timetuple())
    out.write('''\
<?xml version="1.0" encoding="utf-8"?>
<coverage generated="%(stamp).0f" clover="-1.0">
    <project timestamp="%(stamp).0f" name="%(package_name)s">''' % locals())
    out.write('<metrics packages="%(len)d" files="%(len)d" loc="%(all)d" ncloc="%(interesting)d" elements="%(interesting)d" statements="%(interesting)d" coveredelements="%(covered)d" coveredstatements="%(covered)d" complexity="0" classes="0" conditionals="0" coveredconditionals="0" methods="0" coveredmethods="0" />' % {
            'len': len(results), 
            'all': sum(n_all for n_covered, n_interesting, n_all in results.itervalues()), 
            'interesting': sum(n_interesting for n_covered, n_interesting, n_all in results.itervalues()), 
            'covered': sum(n_covered for n_covered, n_interesting, n_all in results.itervalues())})
    for src_file, (n_covered, n_interesting, n_all) in results.iteritems():
        out.write('<package name="%s">' % src_file)
        out.write('<metrics files="1" loc="%(all)d" ncloc="%(interesting)d" elements="%(interesting)d" statements="%(interesting)d" coveredelements="%(covered)d" coveredstatements="%(covered)d" complexity="0" classes="0" conditionals="0" coveredconditionals="0" methods="0" coveredmethods="0" />' % {
                'all': n_all, 'interesting': n_interesting, 'covered': n_covered})
        out.write('</package>')
    out.write('''\
    </project>
    <testproject timestamp="%(stamp).0f" name="%(package_name)s">
        <metrics packages="0" files="0" loc="0" ncloc="0" elements="0" statements="0" coveredelements="0" coveredstatements="0" complexity="0" classes="0" conditionals="0" coveredconditionals="0" methods="0" coveredmethods="0" />
    </testproject>
</coverage>''' % locals())
