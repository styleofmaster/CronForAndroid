/*
 * cron4j - A pure Java cron-like scheduler
 *
 * Copyright (C) 2007-2010 Carlo Pelliccia (www.sauronsoftware.it)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version
 * 2.1, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License 2.1 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License version 2.1 along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package cron4j;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.TreeSet;

/**
 * <p>
 * A UNIX crontab-like pattern is a string split in five space separated parts.
 * Each part is intented as:
 * </p>2
 * <ol>
 * <li><strong>Minutes sub-pattern</strong>. During which minutes of the hour
 * should the task been launched? The values range is from 0 to 59.</li>
 * <li><strong>Hours sub-pattern</strong>. During which hours of the day should
 * the task been launched? The values range is from 0 to 23.</li>
 * <li><strong>Days of month sub-pattern</strong>. During which days of the
 * month should the task been launched? The values range is from 1 to 31. The
 * special value L can be used to recognize the last day of month.</li>
 * <li><strong>Months sub-pattern</strong>. During which months of the year
 * should the task been launched? The values range is from 1 (January) to 12
 * (December), otherwise this sub-pattern allows the aliases &quot;jan&quot;,
 * &quot;feb&quot;, &quot;mar&quot;, &quot;apr&quot;, &quot;may&quot;,
 * &quot;jun&quot;, &quot;jul&quot;, &quot;aug&quot;, &quot;sep&quot;,
 * &quot;oct&quot;, &quot;nov&quot; and &quot;dec&quot;.</li>
 * <li><strong>Days of week sub-pattern</strong>. During which days of the week
 * should the task been launched? The values range is from 0 (Sunday) to 6
 * (Saturday), otherwise this sub-pattern allows the aliases &quot;sun&quot;,
 * &quot;mon&quot;, &quot;tue&quot;, &quot;wed&quot;, &quot;thu&quot;,
 * &quot;fri&quot; and &quot;sat&quot;.</li>
 * </ol>
 * <p>
 * The star wildcard character is also admitted, indicating &quot;every minute
 * of the hour&quot;, &quot;every hour of the day&quot;, &quot;every day of the
 * month&quot;, &quot;every month of the year&quot; and &quot;every day of the
 * week&quot;, according to the sub-pattern in which it is used.
 * </p>
 * <p>
 * Once the scheduler is started, a task will be launched when the five parts in
 * its scheduling pattern will be true at the same time.
 * </p>
 * <p>
 * Some examples:
 * </p>
 * <p>
 * <strong>5 * * * *</strong><br />
 * This pattern causes a task to be launched once every hour, at the begin of
 * the fifth minute (00:05, 01:05, 02:05 etc.).
 * </p>
 * <p>
 * <strong>* * * * *</strong><br />
 * This pattern causes a task to be launched every minute.
 * </p>
 * <p>
 * <strong>* 12 * * Mon</strong><br />
 * This pattern causes a task to be launched every minute during the 12th hour
 * of Monday.
 * </p>
 * <p>
 * <strong>* 12 16 * Mon</strong><br />
 * This pattern causes a task to be launched every minute during the 12th hour
 * of Monday, 16th, but only if the day is the 16th of the month.
 * </p>
 * <p>
 * Every sub-pattern can contain two or more comma separated values.
 * </p>
 * <p>
 * <strong>59 11 * * 1,2,3,4,5</strong><br />
 * This pattern causes a task to be launched at 11:59AM on Monday, Tuesday,
 * Wednesday, Thursday and Friday.
 * </p>
 * <p>
 * Values intervals are admitted and defined using the minus character.
 * </p>
 * <p>
 * <strong>59 11 * * 1-5</strong><br />
 * This pattern is equivalent to the previous one.
 * </p>
 * <p>
 * The slash character can be used to identify step values within a range. It
 * can be used both in the form <em>*&#47;c</em> and <em>a-b/c</em>. The
 * subpattern is matched every <em>c</em> values of the range
 * <em>0,maxvalue</em> or <em>a-b</em>.
 * </p>
 * <p>
 * <strong>*&#47;5 * * * *</strong><br />
 * This pattern causes a task to be launched every 5 minutes (0:00, 0:05, 0:10,
 * 0:15 and so on).
 * </p>
 * <p>
 * <strong>3-18&#47;5 * * * *</strong><br />
 * This pattern causes a task to be launched every 5 minutes starting from the
 * third minute of the hour, up to the 18th (0:03, 0:08, 0:13, 0:18, 1:03, 1:08
 * and so on).
 * </p>
 * <p>
 * <strong>*&#47;15 9-17 * * *</strong><br />
 * This pattern causes a task to be launched every 15 minutes between the 9th
 * and 17th hour of the day (9:00, 9:15, 9:30, 9:45 and so on... note that the
 * last execution will be at 17:45).
 * </p>
 * <p>
 * All the fresh described syntax rules can be used together.
 * </p>
 * <p>
 * <strong>* 12 10-16&#47;2 * *</strong><br />
 * This pattern causes a task to be launched every minute during the 12th hour
 * of the day, but only if the day is the 10th, the 12th, the 14th or the 16th
 * of the month.
 * </p>
 * <p>
 * <strong>* 12 1-15,17,20-25 * *</strong><br />
 * This pattern causes a task to be launched every minute during the 12th hour
 * of the day, but the day of the month must be between the 1st and the 15th,
 * the 20th and the 25, or at least it must be the 17th.
 * </p>
 * <p>
 * Finally cron4j lets you combine more scheduling patterns into one, with the
 * pipe character:
 * </p>
 * <p>
 * <strong>0 5 * * *|8 10 * * *|22 17 * * *</strong><br />
 * This pattern causes a task to be launched every day at 05:00, 10:08 and
 * 17:22.
 * </p>
 *
 * @author Carlo Pelliccia
 * @since 2.0
 */
public class SchedulingPattern {

    /**
     * The parser for the second values.
     */
    private static final ValueParser SECOND_VALUE_PARSER = new SecondValueParser();

    /**
     * The parser for the minute values.
     */
    private static final ValueParser MINUTE_VALUE_PARSER = new MinuteValueParser();

    /**
     * The parser for the hour values.
     */
    private static final ValueParser HOUR_VALUE_PARSER = new HourValueParser();

    /**
     * The parser for the day of month values.
     */
    private static final ValueParser DAY_OF_MONTH_VALUE_PARSER = new DayOfMonthValueParser();

    /**
     * The parser for the month values.
     */
    private static final ValueParser MONTH_VALUE_PARSER = new MonthValueParser();

    /**
     * The parser for the day of week values.
     */
    private static final ValueParser DAY_OF_WEEK_VALUE_PARSER = new DayOfWeekValueParser();

    /**
     * Validates a string as a scheduling pattern.
     *
     * @param schedulingPattern The pattern to validate.
     * @return true if the given string represents a valid scheduling pattern;
     * false otherwise.
     */
    public static boolean validate(String schedulingPattern) {
        try {
            new SchedulingPattern(schedulingPattern);
        } catch (InvalidPatternException e) {
            return false;
        }
        return true;
    }

    /**
     * The pattern as a string.
     */
    private String asString;

    /**
     * The ValueMatcher list for the "minute" field.
     */
    protected ArrayList secondMatchers = new ArrayList();

    /**
     * The ValueMatcher list for the "minute" field.
     */
    protected ArrayList minuteMatchers = new ArrayList();

    /**
     * The ValueMatcher list for the "hour" field.
     */
    protected ArrayList hourMatchers = new ArrayList();

    /**
     * The ValueMatcher list for the "day of month" field.
     */
    protected ArrayList dayOfMonthMatchers = new ArrayList();

    /**
     * The ValueMatcher list for the "month" field.
     */
    protected ArrayList monthMatchers = new ArrayList();

    /**
     * The ValueMatcher list for the "day of week" field.
     */
    protected ArrayList dayOfWeekMatchers = new ArrayList();

    /**
     * How many matcher groups in this pattern?
     */
    protected int matcherSize = 0;

    //add
    protected static final Map<String, Integer> monthMap = new HashMap(20);
    protected static final Map<String, Integer> dayMap = new HashMap(60);
    protected transient int nthdayOfWeek = 0;
    protected transient boolean lastdayOfMonth = false;
    protected transient boolean lastdayOfWeek = false;
    protected static final Integer ALL_SPEC = 99;
    protected static final Integer NO_SPEC = 98;
    public static final int MAX_YEAR;

    /**
     * Builds a SchedulingPattern parsing it from a string.
     *
     * @param pattern The pattern as a crontab-like string.
     * @throws InvalidPatternException If the supplied string is not a valid pattern.
     */
    public SchedulingPattern(String pattern) throws InvalidPatternException {
        this.asString = pattern;
        StringTokenizer st1 = new StringTokenizer(pattern, "|");
        if (st1.countTokens() < 1) {
            throw new InvalidPatternException("invalid pattern: \"" + pattern + "\"");
        }
        while (st1.hasMoreTokens()) {
            String localPattern = st1.nextToken();
            StringTokenizer st2 = new StringTokenizer(localPattern, " \t");
            //notify
//			if (st2.countTokens() != 5) {
//				throw new InvalidPatternException("invalid pattern: \"" + localPattern + "\"");
//			}
            try {
                secondMatchers.add(buildValueMatcher(st2.nextToken(), SECOND_VALUE_PARSER, 0));
            } catch (Exception e) {
                throw new InvalidPatternException("invalid pattern \""
                        + localPattern + "\". Error parsing seconds field: "
                        + e.getMessage() + ".");
            }
            try {
                minuteMatchers.add(buildValueMatcher(st2.nextToken(), MINUTE_VALUE_PARSER, 1));
            } catch (Exception e) {
                throw new InvalidPatternException("invalid pattern \""
                        + localPattern + "\". Error parsing minutes field: "
                        + e.getMessage() + ".");
            }
            try {
                hourMatchers.add(buildValueMatcher(st2.nextToken(), HOUR_VALUE_PARSER, 2));
            } catch (Exception e) {
                throw new InvalidPatternException("invalid pattern \""
                        + localPattern + "\". Error parsing hours field: "
                        + e.getMessage() + ".");
            }
            try {
                String day = st2.nextToken();
                if (day.indexOf(76) != -1 && day.length() > 1 && day.contains(",")) {
                    throw new ParseException("Support for specifying 'L' and 'LW' with other days of the month is not implemented", -1);
                }
                dayOfMonthMatchers.add(buildValueMatcher(day, DAY_OF_MONTH_VALUE_PARSER, 3));
            } catch (Exception e) {
                throw new InvalidPatternException("invalid pattern \""
                        + localPattern
                        + "\". Error parsing days of month field: "
                        + e.getMessage() + ".");
            }
            try {
                monthMatchers.add(buildValueMatcher(st2.nextToken(), MONTH_VALUE_PARSER, 4));
            } catch (Exception e) {
                throw new InvalidPatternException("invalid pattern \""
                        + localPattern + "\". Error parsing months field: "
                        + e.getMessage() + ".");
            }
            try {
                dayOfWeekMatchers.add(buildValueMatcher(st2.nextToken(), DAY_OF_WEEK_VALUE_PARSER, 5));
            } catch (Exception e) {
                throw new InvalidPatternException("invalid pattern \""
                        + localPattern
                        + "\". Error parsing days of week field: "
                        + e.getMessage() + ".");
            }
            matcherSize++;
        }
    }

    private ArrayList values;

    /**
     * A ValueMatcher utility builder.
     *
     * @param str    The pattern part for the ValueMatcher creation.
     * @param parser The parser used to parse the values.
     * @return The requested ValueMatcher.
     * @throws Exception If the supplied pattern part is not valid.
     */
    private ValueMatcher buildValueMatcher(String str, ValueParser parser, int exprOn)
            throws Exception {
        if (str.length() == 1 && str.equals("*")) {
            return new AlwaysTrueValueMatcher();
        }
        values = new ArrayList();
//        ArrayList values = new ArrayList();
        StringTokenizer st = new StringTokenizer(str, ",");
        //modify
//        while (st.hasMoreTokens()) {
//            String element = st.nextToken();
//            ArrayList local;
//            try {
//                local = parseListElement(element, parser);
//            } catch (Exception e) {
//                throw new Exception("invalid field \"" + str
//                        + "\", invalid element \"" + element + "\", "
//                        + e.getMessage());
//            }
//            for (Iterator i = local.iterator(); i.hasNext(); ) {
//                Object value = i.next();
//                if (!values.contains(value)) {
//                    values.add(value);
//                }
//            }
//        }
        while (st.hasMoreTokens()) {
            String v = st.nextToken();
            this.storeExpressionVals(0, v, exprOn);
        }
        if (values.size() == 0) {
            throw new Exception("invalid field \"" + str + "\"");
        }
        if (parser == DAY_OF_MONTH_VALUE_PARSER) {
            return new DayOfMonthValueMatcher(values);
        } else {
            return new IntArrayValueMatcher(values);
        }
    }

    protected int skipWhiteSpace(int i, String s) {
        while (i < s.length() && (s.charAt(i) == ' ' || s.charAt(i) == '\t')) {
            ++i;
        }

        return i;
    }

    protected void addToSet(int val, int end, int incr, int type) throws ParseException {
        if (type != 0 && type != 1) {
            if (type == 2) {
                if ((val < 0 || val > 23 || end > 23) && val != 99) {
                    throw new ParseException("Hour values must be between 0 and 23", -1);
                }
            } else if (type == 3) {
                if ((val < 1 || val > 31 || end > 31) && val != 99 && val != 98) {
                    throw new ParseException("Day of month values must be between 1 and 31", -1);
                }
            } else if (type == 4) {
                if ((val < 1 || val > 12 || end > 12) && val != 99) {
                    throw new ParseException("Month values must be between 1 and 12", -1);
                }
            } else if (type == 5 && (val == 0 || val > 7 || end > 7) && val != 99 && val != 98) {
                throw new ParseException("Day-of-Week values must be between 1 and 7", -1);
            }
        } else if ((val < 0 || val > 59 || end > 59) && val != 99) {
            throw new ParseException("Minute and Second values must be between 0 and 59", -1);
        }

        if ((incr == 0 || incr == -1) && val != 99) {
            if (val != -1) {
                values.add(val);
            } else {
                values.add(NO_SPEC);
            }

        } else {
            int startAt = val;
            int stopAt = end;
            if (val == 99 && incr <= 0) {
                incr = 1;
                values.add(ALL_SPEC);
            }

            if (type != 0 && type != 1) {
                if (type == 2) {
                    if (end == -1) {
                        stopAt = 23;
                    }

                    if (val == -1 || val == 99) {
                        startAt = 0;
                    }
                } else if (type == 3) {
                    if (end == -1) {
                        stopAt = 31;
                    }

                    if (val == -1 || val == 99) {
                        startAt = 1;
                    }
                } else if (type == 4) {
                    if (end == -1) {
                        stopAt = 12;
                    }

                    if (val == -1 || val == 99) {
                        startAt = 1;
                    }
                } else if (type == 5) {
                    if (end == -1) {
                        stopAt = 7;
                    }

                    if (val == -1 || val == 99) {
                        startAt = 1;
                    }
                } else if (type == 6) {
                    if (end == -1) {
                        stopAt = MAX_YEAR;
                    }

                    if (val == -1 || val == 99) {
                        startAt = 1970;
                    }
                }
            } else {
                if (end == -1) {
                    stopAt = 59;
                }

                if (val == -1 || val == 99) {
                    startAt = 0;
                }
            }

            int max = -1;
            if (stopAt < startAt) {
                switch (type) {
                    case 0:
                        max = 60;
                        break;
                    case 1:
                        max = 60;
                        break;
                    case 2:
                        max = 24;
                        break;
                    case 3:
                        max = 31;
                        break;
                    case 4:
                        max = 12;
                        break;
                    case 5:
                        max = 7;
                        break;
                    case 6:
                        throw new IllegalArgumentException("Start year must be less than stop year");
                    default:
                        throw new IllegalArgumentException("Unexpected type encountered");
                }

                stopAt += max;
            }

            for (int i = startAt; i <= stopAt; i += incr) {
                if (max == -1) {
                    values.add(i);
                } else {
                    int i2 = i % max;
                    if (i2 == 0 && (type == 4 || type == 5 || type == 3)) {
                        i2 = max;
                    }

                    values.add(i2);
                }
            }

        }
    }


    static {
        monthMap.put("JAN", 0);
        monthMap.put("FEB", 1);
        monthMap.put("MAR", 2);
        monthMap.put("APR", 3);
        monthMap.put("MAY", 4);
        monthMap.put("JUN", 5);
        monthMap.put("JUL", 6);
        monthMap.put("AUG", 7);
        monthMap.put("SEP", 8);
        monthMap.put("OCT", 9);
        monthMap.put("NOV", 10);
        monthMap.put("DEC", 11);
        dayMap.put("SUN", 1);
        dayMap.put("MON", 2);
        dayMap.put("TUE", 3);
        dayMap.put("WED", 4);
        dayMap.put("THU", 5);
        dayMap.put("FRI", 6);
        dayMap.put("SAT", 7);
        MAX_YEAR = Calendar.getInstance().get(Calendar.YEAR) + 100;
    }

    protected int getMonthNumber(String s) {
        Integer integer = (Integer) monthMap.get(s);
        return integer == null ? -1 : integer;
    }

    protected int getDayOfWeekNumber(String s) {
        Integer integer = (Integer) dayMap.get(s);
        return integer == null ? -1 : integer;
    }

    protected int getNumericValue(String s, int i) {
        int endOfVal = this.findNextWhiteSpace(i, s);
        String val = s.substring(i, endOfVal);
        return Integer.parseInt(val);
    }

    protected int findNextWhiteSpace(int i, String s) {
        while (i < s.length() && (s.charAt(i) != ' ' || s.charAt(i) != '\t')) {
            ++i;
        }
        return i;
    }

    protected ValueSet getValue(int v, String s, int i) {
        char c = s.charAt(i);

        StringBuilder s1;
        for (s1 = new StringBuilder(String.valueOf(v)); c >= '0' && c <= '9'; c = s.charAt(i)) {
            s1.append(c);
            ++i;
            if (i >= s.length()) {
                break;
            }
        }

        ValueSet val = new ValueSet();
        val.pos = i < s.length() ? i : i + 1;
        val.value = Integer.parseInt(s1.toString());
        return val;
    }

    class ValueSet {
        public int value;
        public int pos;

        ValueSet() {
        }
    }

    protected int checkNext(int pos, String s, int val, int type) throws ParseException {
        int end = -1;
        if (pos >= s.length()) {
            this.addToSet(val, end, -1, type);
            return pos;
        } else {
            char c = s.charAt(pos);
            int i;
            TreeSet set;
            if (c == 'L') {
                if (type == 5) {
                    if (val >= 1 && val <= 7) {
                        this.lastdayOfWeek = true;
//                        set = this.getSet(type);
                        values.add(val);
                        i = pos + 1;
                        return i;
                    } else {
                        throw new ParseException("Day-of-Week values must be between 1 and 7", -1);
                    }
                } else {
                    throw new ParseException("'L' option is not valid here. (pos=" + pos + ")", pos);
                }
            } else if (c == 'W') {
                if (type == 3) {
//                    this.nearestWeekday = true;
                    if (val > 31) {
                        throw new ParseException("The 'W' option does not make sense with values larger than 31 (max number of days in a month)", pos);
                    } else {
//                        set = this.getSet(type);
                        values.add(val);
                        i = pos + 1;
                        return i;
                    }
                } else {
                    throw new ParseException("'W' option is not valid here. (pos=" + pos + ")", pos);
                }
            } else if (c != '#') {
                ValueSet vs;
                int v2;
                if (c == '-') {
                    i = pos + 1;
                    c = s.charAt(i);
                    v2 = Integer.parseInt(String.valueOf(c));
                    end = v2;
                    ++i;
                    if (i >= s.length()) {
                        this.addToSet(val, v2, 1, type);
                        return i;
                    } else {
                        c = s.charAt(i);
                        if (c >= '0' && c <= '9') {
                            vs = this.getValue(v2, s, i);
                            end = vs.value;
                            i = vs.pos;
                        }

                        if (i < s.length() && s.charAt(i) == '/') {
                            ++i;
                            c = s.charAt(i);
                            v2 = Integer.parseInt(String.valueOf(c));
                            ++i;
                            if (i >= s.length()) {
                                this.addToSet(val, end, v2, type);
                                return i;
                            } else {
                                c = s.charAt(i);
                                if (c >= '0' && c <= '9') {
                                    vs = this.getValue(v2, s, i);
                                    int v3 = vs.value;
                                    this.addToSet(val, end, v3, type);
                                    i = vs.pos;
                                    return i;
                                } else {
                                    this.addToSet(val, end, v2, type);
                                    return i;
                                }
                            }
                        } else {
                            this.addToSet(val, end, 1, type);
                            return i;
                        }
                    }
                } else if (c == '/') {
                    i = pos + 1;
                    c = s.charAt(i);
                    v2 = Integer.parseInt(String.valueOf(c));
                    ++i;
                    if (i >= s.length()) {
                        this.addToSet(val, end, v2, type);
                        return i;
                    } else {
                        c = s.charAt(i);
                        if (c >= '0' && c <= '9') {
                            vs = this.getValue(v2, s, i);
                            int v3 = vs.value;
                            this.addToSet(val, end, v3, type);
                            i = vs.pos;
                            return i;
                        } else {
                            throw new ParseException("Unexpected character '" + c + "' after '/'", i);
                        }
                    }
                } else {
                    this.addToSet(val, end, 0, type);
                    i = pos + 1;
                    return i;
                }
            } else if (type != 5) {
                throw new ParseException("'#' option is not valid here. (pos=" + pos + ")", pos);
            } else {
                i = pos + 1;

                try {
                    this.nthdayOfWeek = Integer.parseInt(s.substring(i));
                    if (this.nthdayOfWeek < 1 || this.nthdayOfWeek > 5) {
                        throw new Exception();
                    }
                } catch (Exception var12) {
                    throw new ParseException("A numeric value between 1 and 5 must follow the '#' option", i);
                }

//                set = this.getSet(type);
                values.add(val);
                ++i;
                return i;
            }
        }
    }


    protected int storeExpressionVals(int pos, String s, int type) throws ParseException {
        int incr = 0;
        int i = this.skipWhiteSpace(pos, s);
        if (i >= s.length()) {
            return i;
        } else {
            char c = s.charAt(i);
            if (c >= 'A' && c <= 'Z' && !s.equals("L") && !s.equals("LW") && !s.matches("^L-[0-9]*[W]?")) {
                String sub = s.substring(i, i + 3);
                int eval = -1;
                int sval;
                if (type == 4) {
                    sval = this.getMonthNumber(sub) + 1;
                    if (sval <= 0) {
                        throw new ParseException("Invalid Month value: '" + sub + "'", i);
                    }

                    if (s.length() > i + 3) {
                        c = s.charAt(i + 3);
                        if (c == '-') {
                            i += 4;
                            sub = s.substring(i, i + 3);
                            eval = this.getMonthNumber(sub) + 1;
                            if (eval <= 0) {
                                throw new ParseException("Invalid Month value: '" + sub + "'", i);
                            }
                        }
                    }
                } else {
                    if (type != 5) {
                        throw new ParseException("Illegal characters for this position: '" + sub + "'", i);
                    }

                    sval = this.getDayOfWeekNumber(sub);
                    if (sval < 0) {
                        throw new ParseException("Invalid Day-of-Week value: '" + sub + "'", i);
                    }

                    if (s.length() > i + 3) {
                        c = s.charAt(i + 3);
                        if (c == '-') {
                            i += 4;
                            sub = s.substring(i, i + 3);
                            eval = this.getDayOfWeekNumber(sub);
                            if (eval < 0) {
                                throw new ParseException("Invalid Day-of-Week value: '" + sub + "'", i);
                            }
                        } else if (c == '#') {
                            try {
                                i += 4;
                                this.nthdayOfWeek = Integer.parseInt(s.substring(i));
                                if (this.nthdayOfWeek < 1 || this.nthdayOfWeek > 5) {
                                    throw new Exception();
                                }
                            } catch (Exception var11) {
                                throw new ParseException("A numeric value between 1 and 5 must follow the '#' option", i);
                            }
                        } else if (c == 'L') {
                            this.lastdayOfWeek = true;
                            ++i;
                        }
                    }
                }

                if (eval != -1) {
                    incr = 1;
                }

                this.addToSet(sval, eval, incr, type);
                return i + 3;
            } else {
                int val;
                if (c == '?') {
                    ++i;
                    if (i + 1 < s.length() && s.charAt(i) != ' ' && s.charAt(i + 1) != '\t') {
                        throw new ParseException("Illegal character after '?': " + s.charAt(i), i);
                    } else if (type != 5 && type != 3) {
                        throw new ParseException("'?' can only be specfied for Day-of-Month or Day-of-Week.", i);
                    } else {
                        if (type == 5 && !this.lastdayOfMonth) {
//                            val = (Integer)this.daysOfMonth.last();
//                            if (val == 98) {
//                                throw new ParseException("'?' can only be specfied for Day-of-Month -OR- Day-of-Week.", i);
//                            }
                        }

                        this.addToSet(98, -1, 0, type);
                        return i;
                    }
                } else if (c != '*' && c != '/') {
                    if (c == 'L') {
                        ++i;
                        if (type == 3) {
                            this.lastdayOfMonth = true;
                        }

                        if (type == 5) {
                            this.addToSet(7, 7, 0, type);
                        }

                        if (type == 3 && s.length() > i) {
                            c = s.charAt(i);
                            if (c == '-') {
                                ValueSet vs = this.getValue(0, s, i + 1);
//                                this.lastdayOffset = vs.value;
                                if (vs.value > 30) {
                                    throw new ParseException("Offset from last day must be <= 30", i + 1);
                                }

                                i = vs.pos;
                            }

                            if (s.length() > i) {
                                c = s.charAt(i);
                                if (c == 'W') {
//                                    this.nearestWeekday = true;
                                    ++i;
                                }
                            }
                        }

                        return i;
                    } else if (c >= '0' && c <= '9') {
                        val = Integer.parseInt(String.valueOf(c));
                        ++i;
                        if (i >= s.length()) {
                            this.addToSet(val, -1, -1, type);
                            return i;
                        } else {
                            c = s.charAt(i);
                            if (c >= '0' && c <= '9') {
                                ValueSet vs = this.getValue(val, s, i);
                                val = vs.value;
                                i = vs.pos;
                            }

                            i = this.checkNext(i, s, val, type);
                            return i;
                        }
                    } else {
                        throw new ParseException("Unexpected character: " + c, i);
                    }
                } else if (c == '*' && i + 1 >= s.length()) {
                    this.addToSet(99, -1, incr, type);
                    return i + 1;
                } else if (c == '/' && (i + 1 >= s.length() || s.charAt(i + 1) == ' ' || s.charAt(i + 1) == '\t')) {
                    throw new ParseException("'/' must be followed by an integer.", i);
                } else {
                    if (c == '*') {
                        ++i;
                    }

                    c = s.charAt(i);
//                    int incr;
                    if (c != '/') {
                        incr = 1;
                    } else {
                        ++i;
                        if (i >= s.length()) {
                            throw new ParseException("Unexpected end of string.", i);
                        }

                        incr = this.getNumericValue(s, i);
                        ++i;
                        if (incr > 10) {
                            ++i;
                        }

                        if (incr > 59 && (type == 0 || type == 1)) {
                            throw new ParseException("Increment > 60 : " + incr, i);
                        }

                        if (incr > 23 && type == 2) {
                            throw new ParseException("Increment > 24 : " + incr, i);
                        }

                        if (incr > 31 && type == 3) {
                            throw new ParseException("Increment > 31 : " + incr, i);
                        }

                        if (incr > 7 && type == 5) {
                            throw new ParseException("Increment > 7 : " + incr, i);
                        }

                        if (incr > 12 && type == 4) {
                            throw new ParseException("Increment > 12 : " + incr, i);
                        }
                    }

                    this.addToSet(99, -1, incr, type);
                    return i;
                }
            }
        }
    }


    /**
     * Parses an element of a list of values of the pattern.
     *
     * @param str    The element string.
     * @param parser The parser used to parse the values.
     * @return A list of integers representing the allowed values.
     * @throws Exception If the supplied pattern part is not valid.
     */
    private ArrayList parseListElement(String str, ValueParser parser)
            throws Exception {
        StringTokenizer st = new StringTokenizer(str, "/");
        int size = st.countTokens();
        if (size < 1 || size > 2) {
            throw new Exception("syntax error");
        }
        ArrayList values;
        try {
            values = parseRange(st.nextToken(), parser);
        } catch (Exception e) {
            throw new Exception("invalid range, " + e.getMessage());
        }
        if (size == 2) {
            String dStr = st.nextToken();
            int div;
            try {
                div = Integer.parseInt(dStr);
            } catch (NumberFormatException e) {
                throw new Exception("invalid divisor \"" + dStr + "\"");
            }
            if (div < 1) {
                throw new Exception("non positive divisor \"" + div + "\"");
            }
            ArrayList values2 = new ArrayList();
            for (int i = 0; i < values.size(); i += div) {
                values2.add(values.get(i));
            }
            return values2;
        } else {
            return values;
        }
    }

    /**
     * Parses a range of values.
     *
     * @param str    The range string.
     * @param parser The parser used to parse the values.
     * @return A list of integers representing the allowed values.
     * @throws Exception If the supplied pattern part is not valid.
     */
    private ArrayList parseRange(String str, ValueParser parser)
            throws Exception {
        if (str.equals("*")) {
            int min = parser.getMinValue();
            int max = parser.getMaxValue();
            ArrayList values = new ArrayList();
            for (int i = min; i <= max; i++) {
                values.add(new Integer(i));
            }
            return values;
        }
        StringTokenizer st = new StringTokenizer(str, "-");
        int size = st.countTokens();
        if (size < 1 || size > 2) {
            throw new Exception("syntax error");
        }
        String v1Str = st.nextToken();
        int v1;
        try {
            v1 = parser.parse(v1Str);
        } catch (Exception e) {
            throw new Exception("invalid value \"" + v1Str + "\", "
                    + e.getMessage());
        }
        if (size == 1) {
            ArrayList values = new ArrayList();
            values.add(new Integer(v1));
            return values;
        } else {
            String v2Str = st.nextToken();
            int v2;
            try {
                v2 = parser.parse(v2Str);
            } catch (Exception e) {
                throw new Exception("invalid value \"" + v2Str + "\", "
                        + e.getMessage());
            }
            ArrayList values = new ArrayList();
            if (v1 < v2) {
                for (int i = v1; i <= v2; i++) {
                    values.add(new Integer(i));
                }
            } else if (v1 > v2) {
                int min = parser.getMinValue();
                int max = parser.getMaxValue();
                for (int i = v1; i <= max; i++) {
                    values.add(new Integer(i));
                }
                for (int i = min; i <= v2; i++) {
                    values.add(new Integer(i));
                }
            } else {
                // v1 == v2
                values.add(new Integer(v1));
            }
            return values;
        }
    }

    /**
     * This methods returns true if the given timestamp (expressed as a UNIX-era
     * millis value) matches the pattern, according to the given time zone.
     *
     * @param timezone A time zone.
     * @param millis   The timestamp, as a UNIX-era millis value.
     * @return true if the given timestamp matches the pattern.
     */
    public boolean match(TimeZone timezone, long millis) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTimeInMillis(millis);
        gc.setTimeZone(timezone);
        int second = gc.get(Calendar.SECOND);
        int minute = gc.get(Calendar.MINUTE);
        int hour = gc.get(Calendar.HOUR_OF_DAY);
        int dayOfMonth = gc.get(Calendar.DAY_OF_MONTH);
        int month = gc.get(Calendar.MONTH) + 1;
        int dayOfWeek = gc.get(Calendar.DAY_OF_WEEK) - 1;
        int year = gc.get(Calendar.YEAR);
        for (int i = 0; i < matcherSize; i++) {
            ValueMatcher secondMatcher = (ValueMatcher) secondMatchers.get(i);
            ValueMatcher minuteMatcher = (ValueMatcher) minuteMatchers.get(i);
            ValueMatcher hourMatcher = (ValueMatcher) hourMatchers.get(i);
            ValueMatcher dayOfMonthMatcher = (ValueMatcher) dayOfMonthMatchers.get(i);
            ValueMatcher monthMatcher = (ValueMatcher) monthMatchers.get(i);
            ValueMatcher dayOfWeekMatcher = (ValueMatcher) dayOfWeekMatchers.get(i);
            boolean eval = secondMatcher.match(second)
                    && minuteMatcher.match(minute)
                    && hourMatcher.match(hour)
                    && ((dayOfMonthMatcher instanceof DayOfMonthValueMatcher) ? ((DayOfMonthValueMatcher) dayOfMonthMatcher)
                    .match(dayOfMonth, month, gc.isLeapYear(year))
                    : dayOfMonthMatcher.match(dayOfMonth))
                    && monthMatcher.match(month)
                    && dayOfWeekMatcher.match(dayOfWeek);
            if (eval) {
                return true;
            }
        }
        return false;
    }

    /**
     * This methods returns true if the given timestamp (expressed as a UNIX-era
     * millis value) matches the pattern, according to the system default time
     * zone.
     *
     * @param millis The timestamp, as a UNIX-era millis value.
     * @return true if the given timestamp matches the pattern.
     */
    public boolean match(long millis) {
        return match(TimeZone.getDefault(), millis);
    }

    /**
     * Returns the pattern as a string.
     *
     * @return The pattern as a string.
     */
    public String toString() {
        return asString;
    }

    /**
     * This utility method changes an alias to an int value.
     *
     * @param value   The value.
     * @param aliases The aliases list.
     * @param offset  The offset appplied to the aliases list indices.
     * @return The parsed value.
     * @throws Exception If the expressed values doesn't match any alias.
     */
    private static int parseAlias(String value, String[] aliases, int offset)
            throws Exception {
        for (int i = 0; i < aliases.length; i++) {
            if (aliases[i].equalsIgnoreCase(value)) {
                return offset + i;
            }
        }
        throw new Exception("invalid alias \"" + value + "\"");
    }

    /**
     * Definition for a value parser.
     */
    private static interface ValueParser {

        /**
         * Attempts to parse a value.
         *
         * @param value The value.
         * @return The parsed value.
         * @throws Exception If the value can't be parsed.
         */
        public int parse(String value) throws Exception;

        /**
         * Returns the minimum value accepred by the parser.
         *
         * @return The minimum value accepred by the parser.
         */
        public int getMinValue();

        /**
         * Returns the maximum value accepred by the parser.
         *
         * @return The maximum value accepred by the parser.
         */
        public int getMaxValue();

    }

    /**
     * A simple value parser.
     */
    private static class SimpleValueParser implements ValueParser {

        /**
         * The minimum allowed value.
         */
        protected int minValue;

        /**
         * The maximum allowed value.
         */
        protected int maxValue;

        /**
         * Builds the value parser.
         *
         * @param minValue The minimum allowed value.
         * @param maxValue The maximum allowed value.
         */
        public SimpleValueParser(int minValue, int maxValue) {
            this.minValue = minValue;
            this.maxValue = maxValue;
        }

        public int parse(String value) throws Exception {
            int i;
            try {
                i = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new Exception("invalid integer value");
            }
            if (i < minValue || i > maxValue) {
                throw new Exception("value out of range");
            }
            return i;
        }

        public int getMinValue() {
            return minValue;
        }

        public int getMaxValue() {
            return maxValue;
        }

    }

    /**
     * The minutes value parser.
     */
    private static class SecondValueParser extends SimpleValueParser {

        /**
         * Builds the value parser.
         */
        public SecondValueParser() {
            super(0, 59);
        }

    }


    /**
     * The minutes value parser.
     */
    private static class MinuteValueParser extends SimpleValueParser {

        /**
         * Builds the value parser.
         */
        public MinuteValueParser() {
            super(0, 59);
        }

    }

    /**
     * The hours value parser.
     */
    private static class HourValueParser extends SimpleValueParser {

        /**
         * Builds the value parser.
         */
        public HourValueParser() {
            super(0, 23);
        }

    }

    /**
     * The days of month value parser.
     */
    private static class DayOfMonthValueParser extends SimpleValueParser {

        /**
         * Builds the value parser.
         */
        public DayOfMonthValueParser() {
            super(1, 31);
        }

        /**
         * Added to support last-day-of-month.
         *
         * @param value The value to be parsed
         * @return the integer day of the month or 32 for last day of the month
         * @throws Exception if the input value is invalid
         */
        public int parse(String value) throws Exception {
            if (value.equalsIgnoreCase("L")) {
                return 32;
            } else {
                return super.parse(value);
            }
        }

    }

    /**
     * The value parser for the months field.
     */
    private static class MonthValueParser extends SimpleValueParser {

        /**
         * Months aliases.
         */
        private static String[] ALIASES = {"jan", "feb", "mar", "apr", "may",
                "jun", "jul", "aug", "sep", "oct", "nov", "dec"};

        /**
         * Builds the months value parser.
         */
        public MonthValueParser() {
            super(1, 12);
        }

        public int parse(String value) throws Exception {
            try {
                // try as a simple value
                return super.parse(value);
            } catch (Exception e) {
                // try as an alias
                return parseAlias(value, ALIASES, 1);
            }
        }

    }

    /**
     * The value parser for the months field.
     */
    private static class DayOfWeekValueParser extends SimpleValueParser {

        /**
         * Days of week aliases.
         */
        private static String[] ALIASES = {"sun", "mon", "tue", "wed", "thu", "fri", "sat"};

        /**
         * Builds the months value parser.
         */
        public DayOfWeekValueParser() {
            super(0, 7);
        }

        public int parse(String value) throws Exception {
            try {
                // try as a simple value
                return super.parse(value) % 7;
            } catch (Exception e) {
                // try as an alias
                return parseAlias(value, ALIASES, 0);
            }
        }

    }

}
