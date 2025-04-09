package javaframework.applayer.security.validation;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javaframework.base.exceptions.FrameworkRuntimeException;

/**
 * Represents a validation operation over a string. Validation is performed by comparison of the
 * string against a regular expression.
 *
 * <br/><br/>
 *
 * <b><u>Dependencies</u></b><br/>
 * Base
 * <br/><br/>
 * 
 * <b><u>Design notes</u></b><br/>
 *
 * There are some typical regular expressions defined in interface <code>InterfaceValidation</code>.
 * <br/>
 * The <code>String</code> class of the JDK includes the method <code>matches</code> that performs similar function.
 *
 * <br/><br/>
 * <b>· Creation time:</b> 01/01/2007<br/>
 * <b>· Revisions:</b> 02/05/2010<br/><br/>
 * <b><u>State</u></b><br/>
 * <b>· Debugged:</b> No<br/>
 * <b>· Structural tests:</b> -<br/>
 * <b>· Functional tests:</b> -<br/>
 *
 * @author Francisco Pérez R. de V. (franjfw@yahoo.es)
 * @version JavaFramework.0.1.0.en
 * @version Validación.0.0.2
 * @since JavaFramework.0.0.0.en
 * @see <a href=””></a>
 *
 */
public final class Validator implements InterfaceValidator
	{

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Boolean isValid(final TypesOfValidation typeOfValidation, final String stringToValidate)
		{
		final String REG_EX = typeOfValidation.getValue();
		return this.isValid(REG_EX, stringToValidate);
		}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Boolean isValid(final String regExPattern, final String stringToValidate)
		{
		final Pattern COMPARISON_PATTERN = Pattern.compile(regExPattern);
		final Matcher COMPARATOR = COMPARISON_PATTERN.matcher(stringToValidate);
		return COMPARATOR.matches();
		}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final ArrayList<Occurrence> findPattern(final String regExPatternToFind, final String stringToParse)
		{
		final Pattern COMPARISON_PATTERN = Pattern.compile(regExPatternToFind);
		final Matcher COMPARATOR = COMPARISON_PATTERN.matcher(stringToParse);

		final ArrayList<Occurrence> OCCURRENCE_LIST = new ArrayList<>();

		while (COMPARATOR.find())
			{
			final String foundString = COMPARATOR.group();
			final int startPosition = COMPARATOR.start();
			final int endPosition = COMPARATOR.end();

			final Occurrence occurrence = new Occurrence(foundString, startPosition, endPosition);

			OCCURRENCE_LIST.add(occurrence);
			}
		return OCCURRENCE_LIST;
		}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String replaceMasks(final String stringToParse, final String[] regExMasks, final String[] values) throws FrameworkRuntimeException
		{
		String aux = stringToParse;
		if ((regExMasks != null) && (values != null))
			{
			if (regExMasks.length == values.length)
				{
				for (int i = 0; i < regExMasks.length; i++)
					{
					if (values[i] != null)
						{
						aux = aux.replaceFirst(regExMasks[i], values[i]);
						}
					}
				}
			else
				{
				final String EXCEPTION_MSG = "The arrays' length does not match.";
				throw new FrameworkRuntimeException(EXCEPTION_MSG, stringToParse, regExMasks, values);
				}
			}
		return aux;
		}
	}
